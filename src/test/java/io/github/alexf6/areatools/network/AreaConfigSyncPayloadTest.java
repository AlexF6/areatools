package io.github.alexf6.areatools.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.github.alexf6.areatools.AreaToolsConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class AreaConfigSyncPayloadTest {

    @AfterEach
    void reset() {
        SyncedAreaConfig.clear();
    }

    private static AreaConfigSyncPayload roundTrip(AreaConfigSyncPayload payload) {
        ByteBuf buf = Unpooled.buffer();
        AreaConfigSyncPayload.CODEC.encode(buf, payload);
        AreaConfigSyncPayload read = AreaConfigSyncPayload.CODEC.decode(buf);
        assertEquals(0, buf.readableBytes(), "no deben sobrar bytes");
        return read;
    }

    @Test
    void roundTripKeepsTheThreeSizes() {
        AreaConfigSyncPayload payload = new AreaConfigSyncPayload(2, 3, 5);
        assertEquals(payload, roundTrip(payload));
    }

    @Test
    void payloadBuiltFromTheServerConfigCarriesItsSizes() {
        AreaToolsConfig server = new AreaToolsConfig(3, 1, 4, 70.0F, 2, true);
        AreaConfigSyncPayload payload = AreaConfigSyncPayload.of(server);
        assertEquals(new AreaConfigSyncPayload(3, 1, 4), payload);
    }

    @Test
    void applyingItKeepsTheLocalNonAreaSettings() {
        AreaToolsConfig local = new AreaToolsConfig(1, 2, 3, 12.0F, 5, false);
        AreaToolsConfig merged = new AreaConfigSyncPayload(3, 3, 5).applyTo(local);
        assertEquals(3, merged.radius());
        assertEquals(3, merged.wideRadius());
        assertEquals(5, merged.deepDepth());
        assertEquals(12.0F, merged.netheriteMiningSpeed());
        assertEquals(5, merged.durabilityDivisor());
        assertEquals(false, merged.centerDrops());
    }

    @Test
    void aHostileServerCannotExceedTheLimits() {
        AreaToolsConfig merged = new AreaConfigSyncPayload(1_000_000, -5, Integer.MAX_VALUE)
                .applyTo(AreaToolsConfig.DEFAULTS);
        assertEquals(AreaToolsConfig.MAX_RADIUS, merged.radius());
        assertEquals(1, merged.wideRadius());
        assertEquals(AreaToolsConfig.MAX_DEPTH, merged.deepDepth());
    }

    @Test
    void theSyncedConfigWinsUntilItIsCleared() {
        AreaToolsConfig local = AreaToolsConfig.DEFAULTS;
        assertSame(local, SyncedAreaConfig.effective(local), "sin conexión manda la local");

        AreaToolsConfig server = new AreaToolsConfig(3, 3, 5, 70.0F, 2, true);
        SyncedAreaConfig.set(server);
        assertSame(server, SyncedAreaConfig.effective(local));

        SyncedAreaConfig.clear();
        assertSame(local, SyncedAreaConfig.effective(local));
    }
}
