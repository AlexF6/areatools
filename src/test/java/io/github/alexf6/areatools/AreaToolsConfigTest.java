package io.github.alexf6.areatools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AreaToolsConfigTest {

    @TempDir
    Path dir;

    @Test
    void outOfRangeValuesAreClamped() {
        AreaToolsConfig config = new AreaToolsConfig(99, 99, 0, 99999.0F, -4, true);

        assertEquals(AreaToolsConfig.MAX_RADIUS, config.radius());
        assertEquals(AreaToolsConfig.MAX_RADIUS, config.wideRadius());
        assertEquals(1, config.deepDepth());
        assertEquals(AreaToolsConfig.MAX_SPEED, config.netheriteMiningSpeed());
        assertEquals(1, config.durabilityDivisor());
    }

    @Test
    void missingFileIsCreatedWithDefaults() {
        Path file = dir.resolve("areatools.json");

        assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file));
        assertTrue(Files.exists(file));
        assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file), "lo escrito se puede volver a leer igual");
    }

    @Test
    void missingKeysKeepTheirDefaults() throws IOException {
        Path file = dir.resolve("areatools.json");
        Files.writeString(file, "{ \"radius\": 2, \"centerDrops\": false }");

        AreaToolsConfig config = AreaToolsConfig.load(file);

        assertEquals(2, config.radius());
        assertFalse(config.centerDrops());
        assertEquals(AreaToolsConfig.DEFAULTS.deepDepth(), config.deepDepth());
        assertEquals(AreaToolsConfig.DEFAULTS.durabilityDivisor(), config.durabilityDivisor());
    }

    @Test
    void brokenFileFallsBackToDefaultsAndIsLeftUntouched() throws IOException {
        Path file = dir.resolve("areatools.json");
        Files.writeString(file, "{ esto no es json");

        assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file));
        assertEquals("{ esto no es json", Files.readString(file));
    }

    @Test
    void shapeFollowsTheMode() {
        AreaToolsConfig config = new AreaToolsConfig(1, 2, 4, 70.0F, 2, true);

        assertEquals(new AreaShape(1, 1), AreaShape.of(AreaMode.FLAT, config));
        assertEquals(new AreaShape(2, 1), AreaShape.of(AreaMode.WIDE, config));
        assertEquals(new AreaShape(1, 4), AreaShape.of(AreaMode.DEEP, config));
    }

    @Test
    void modeCyclesThroughAllValues() {
        assertEquals(AreaMode.WIDE, AreaMode.FLAT.next());
        assertEquals(AreaMode.DEEP, AreaMode.WIDE.next());
        assertEquals(AreaMode.FLAT, AreaMode.DEEP.next());
    }
}
