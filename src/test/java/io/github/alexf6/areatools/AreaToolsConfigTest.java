package io.github.alexf6.areatools;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    void missingFileGetsCreatedWithDefaults() {
        Path file = dir.resolve("areatools.json");

        assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file));
        assertTrue(Files.exists(file));
    }

    @Test
    void valuesAreClampedToAllowedRanges() {
        AreaToolsConfig zero = new AreaToolsConfig(0, 0, 0, 0.0F, 0, true);
        assertEquals(1, zero.radius());
        assertEquals(1, zero.wideRadius());
        assertEquals(1, zero.deepDepth());
        assertEquals(1.0F, zero.netheriteMiningSpeed());
        assertEquals(1, zero.durabilityDivisor());

        AreaToolsConfig negative = new AreaToolsConfig(-10, -5, -2, -100.0F, -1, false);
        assertEquals(1, negative.radius());
        assertEquals(1, negative.wideRadius());
        assertEquals(1, negative.deepDepth());
        assertEquals(1.0F, negative.netheriteMiningSpeed());
        assertEquals(1, negative.durabilityDivisor());

        AreaToolsConfig tooBig = new AreaToolsConfig(99, 99, 99, 99999.0F, 99, true);
        assertEquals(AreaToolsConfig.MAX_RADIUS, tooBig.radius());
        assertEquals(AreaToolsConfig.MAX_RADIUS, tooBig.wideRadius());
        assertEquals(AreaToolsConfig.MAX_DEPTH, tooBig.deepDepth());
        assertEquals(AreaToolsConfig.MAX_SPEED, tooBig.netheriteMiningSpeed());
        assertEquals(AreaToolsConfig.MAX_DIVISOR, tooBig.durabilityDivisor());
    }

    @Test
    void nanMiningSpeedFallsBackToDefault() {
        AreaToolsConfig config = new AreaToolsConfig(1, 2, 3, Float.NaN, 2, true);
        assertEquals(AreaToolsConfig.DEFAULTS.netheriteMiningSpeed(), config.netheriteMiningSpeed());
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
    void brokenFileIsBackedUpAsBakAndRegenerated() throws IOException {
        Path file = dir.resolve("areatools.json");
        Path backup = dir.resolve("areatools.json.bak");
        Files.writeString(file, "{ esto no es json");

        assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file));
        assertTrue(Files.exists(backup));
        assertEquals("{ esto no es json", Files.readString(backup));
        assertTrue(Files.exists(file));
        assertNotEquals("{ esto no es json", Files.readString(file));
    }

    @Test
    void unreadableFileReturnsDefaultsWithoutModifyingOrBackingUp() throws IOException {
        // Un directorio con el nombre del archivo: leerlo lanza IOException en cualquier sistema y sin depender
        // de permisos (como root o en Windows, quitar el permiso de lectura no impide leer)
        Path file = dir.resolve("areatools.json");
        Files.createDirectories(file.resolve("contenido"));

        assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file));
        assertTrue(Files.isDirectory(file), "no se tocó");
        assertFalse(Files.exists(dir.resolve("areatools.json.bak")));
    }

    @Test
    void emptyFileAndEmptyObjectUseDefaultsWithoutBackup() throws IOException {
        for (String content : new String[] {"", "   \n", "{}"}) {
            Path file = dir.resolve("areatools.json");
            Files.writeString(file, content);

            assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file), "contenido: '" + content + "'");
            assertFalse(Files.exists(dir.resolve("areatools.json.bak")), "contenido: '" + content + "'");
        }
    }

    @Test
    void wrongValueTypesAndRootsAreTreatedAsBrokenFiles() throws IOException {
        for (String content : new String[] {"{ \"radius\": \"grande\" }", "{ \"centerDrops\": 5 }", "[1, 2]", "\"hola\"",
                "{ \"radius\": {} }"}) {
            Path file = dir.resolve("areatools.json");
            Path backup = dir.resolve("areatools.json.bak");
            Files.deleteIfExists(backup);
            Files.writeString(file, content);

            assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file), content);
            assertEquals(content, Files.readString(backup), "respaldado: " + content);
            assertNotEquals(content, Files.readString(file), "regenerado: " + content);
        }
    }

    @Test
    void bytesThatAreNotUtf8AreTreatedAsABrokenFile() throws IOException {
        Path file = dir.resolve("areatools.json");
        byte[] garbage = {'{', (byte) 0xC3, (byte) 0x28, '}'};
        Files.write(file, garbage);

        assertEquals(AreaToolsConfig.DEFAULTS, AreaToolsConfig.load(file));
        assertArrayEquals(garbage, Files.readAllBytes(dir.resolve("areatools.json.bak")));
    }

    @Test
    void aNewBrokenFileReplacesAnOlderBackup() throws IOException {
        Path file = dir.resolve("areatools.json");
        Path backup = dir.resolve("areatools.json.bak");
        Files.writeString(backup, "respaldo viejo");
        Files.writeString(file, "{ roto");

        AreaToolsConfig.load(file);

        assertEquals("{ roto", Files.readString(backup), "solo se conserva el último respaldo");
    }

    @Test
    void outOfRangeValuesInTheFileAreClamped() throws IOException {
        Path file = dir.resolve("areatools.json");
        Files.writeString(file, "{ \"radius\": 99, \"deepDepth\": -3, \"durabilityDivisor\": 0 }");

        AreaToolsConfig config = AreaToolsConfig.load(file);

        assertEquals(AreaToolsConfig.MAX_RADIUS, config.radius());
        assertEquals(1, config.deepDepth());
        assertEquals(1, config.durabilityDivisor());
        assertFalse(Files.exists(dir.resolve("areatools.json.bak")), "un valor fuera de rango no es un archivo roto");
    }

    @Test
    void shapeFollowsTheMode() {
        AreaToolsConfig config = new AreaToolsConfig(1, 2, 4, 70.0F, 2, true);

        assertEquals(new AreaShape(1, 1), AreaMode.FLAT.shape(config));
        assertEquals(new AreaShape(2, 1), AreaMode.WIDE.shape(config));
        assertEquals(new AreaShape(1, 4), AreaMode.DEEP.shape(config));
    }

    @Test
    void modeCyclesThroughAllValues() {
        assertEquals(AreaMode.WIDE, AreaMode.FLAT.next());
        assertEquals(AreaMode.DEEP, AreaMode.WIDE.next());
        assertEquals(AreaMode.FLAT, AreaMode.DEEP.next());
    }

    @Test
    void defaultValuesMatchExpectations() {
        assertEquals(1, AreaToolsConfig.DEFAULTS.radius());
        assertEquals(2, AreaToolsConfig.DEFAULTS.wideRadius());
        assertEquals(3, AreaToolsConfig.DEFAULTS.deepDepth());
        assertEquals(70.0F, AreaToolsConfig.DEFAULTS.netheriteMiningSpeed());
        assertEquals(2, AreaToolsConfig.DEFAULTS.durabilityDivisor());
        assertTrue(AreaToolsConfig.DEFAULTS.centerDrops());
    }
}
