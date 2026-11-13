package io.github.alexf6.areatools.network;

import io.github.alexf6.areatools.AreaToolsConfig;
import io.github.alexf6.areatools.AreaToolsMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Servidor → cliente, al conectarse: el tamaño del área según el servidor. El servidor es quien rompe los bloques,
 * pero el contorno lo dibuja el cliente; sin esto, un {@code areatools.json} distinto en el cliente dibujaría un
 * contorno que no corresponde a lo que se rompe. Los valores se acotan en el cliente al construir la config.
 */
public record AreaConfigSyncPayload(int radius, int wideRadius, int deepDepth) implements CustomPacketPayload {

    public static final Type<AreaConfigSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(AreaToolsMod.MOD_ID, "area_config"));

    public static final StreamCodec<ByteBuf, AreaConfigSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AreaConfigSyncPayload::radius,
            ByteBufCodecs.VAR_INT, AreaConfigSyncPayload::wideRadius,
            ByteBufCodecs.VAR_INT, AreaConfigSyncPayload::deepDepth,
            AreaConfigSyncPayload::new);

    public static AreaConfigSyncPayload of(AreaToolsConfig config) {
        return new AreaConfigSyncPayload(config.radius(), config.wideRadius(), config.deepDepth());
    }

    /**
     * La config local con el tamaño del servidor. Los valores fuera de rango se acotan (y avisan en el log) en el
     * constructor de {@link AreaToolsConfig}, así que un servidor hostil no consigue áreas mayores que los topes.
     */
    public AreaToolsConfig applyTo(AreaToolsConfig local) {
        return new AreaToolsConfig(radius, wideRadius, deepDepth, local.netheriteMiningSpeed(),
                local.durabilityDivisor(), local.centerDrops());
    }

    @Override
    public Type<AreaConfigSyncPayload> type() {
        return TYPE;
    }
}
