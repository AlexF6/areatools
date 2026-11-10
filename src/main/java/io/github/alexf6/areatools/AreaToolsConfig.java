package io.github.alexf6.areatools;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Configuración del mod ({@code config/areatools.json}). Inmutable: los valores fuera de rango se corrigen
 * al construirla, así el resto del código puede confiar en ellos.
 *
 * @param radius               mitad del lado del cuadrado en modo plano y profundo: 1 = 3x3, 2 = 5x5, 3 = 7x7
 * @param wideRadius           mitad del lado del cuadrado en modo ancho (por defecto 2 = 5x5)
 * @param deepDepth            bloques de profundidad en modo DEEP (incluye el golpeado)
 * @param netheriteMiningSpeed velocidad de minado del netherita (el de vanilla es 9)
 * @param durabilityDivisor    cada N bloques extra rotos cuestan 1 de durabilidad (1 = vanilla, 1 por bloque)
 * @param centerDrops          si los drops de los bloques vecinos se juntan en el bloque golpeado
 */
public record AreaToolsConfig(int radius, int wideRadius, int deepDepth, float netheriteMiningSpeed, int durabilityDivisor,
                           boolean centerDrops) {

    public static final int MAX_RADIUS = 3;
    public static final int MAX_DEPTH = 5;
    public static final int MAX_DIVISOR = 9;
    public static final float MAX_SPEED = 1000.0F;

    public static final AreaToolsConfig DEFAULTS = new AreaToolsConfig(1, 2, 3, 70.0F, 2, true);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public AreaToolsConfig {
        radius = Math.clamp(radius, 1, MAX_RADIUS);
        wideRadius = Math.clamp(wideRadius, 1, MAX_RADIUS);
        deepDepth = Math.clamp(deepDepth, 1, MAX_DEPTH);
        netheriteMiningSpeed = Math.clamp(netheriteMiningSpeed, 1.0F, MAX_SPEED);
        durabilityDivisor = Math.clamp(durabilityDivisor, 1, MAX_DIVISOR);
    }

    /** Se carga una sola vez, la primera vez que se pide (antes de registrar los ítems). */
    public static AreaToolsConfig get() {
        return Holder.INSTANCE;
    }

    /**
     * Lee la configuración de {@code file}. Si no existe la crea con los valores por defecto; si está rota
     * usa los valores por defecto sin tocar el archivo, para que el jugador pueda corregirlo.
     */
    static AreaToolsConfig load(Path file) {
        if (!Files.exists(file)) {
            write(file, DEFAULTS);
            return DEFAULTS;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            Raw raw = GSON.fromJson(reader, Raw.class);
            return raw == null ? DEFAULTS : raw.toConfig();
        } catch (IOException | JsonParseException e) {
            AreaToolsMod.LOGGER.warn("No se pudo leer {}; se usan los valores por defecto.", file, e);
            return DEFAULTS;
        }
    }

    private static void write(Path file, AreaToolsConfig config) {
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(new Raw(config), writer);
        } catch (IOException e) {
            AreaToolsMod.LOGGER.warn("No se pudo crear {}.", file, e);
        }
    }

    private static final class Holder {
        static final AreaToolsConfig INSTANCE =
                load(FabricLoader.getInstance().getConfigDir().resolve(AreaToolsMod.MOD_ID + ".json"));
    }

    /** Forma del JSON. Los valores por defecto de los campos cubren las claves que falten en el archivo. */
    private static final class Raw {
        int radius = DEFAULTS.radius;
        int wideRadius = DEFAULTS.wideRadius;
        int deepDepth = DEFAULTS.deepDepth;
        float netheriteMiningSpeed = DEFAULTS.netheriteMiningSpeed;
        int durabilityDivisor = DEFAULTS.durabilityDivisor;
        boolean centerDrops = DEFAULTS.centerDrops;

        Raw() {}

        Raw(AreaToolsConfig config) {
            radius = config.radius;
            wideRadius = config.wideRadius;
            deepDepth = config.deepDepth;
            netheriteMiningSpeed = config.netheriteMiningSpeed;
            durabilityDivisor = config.durabilityDivisor;
            centerDrops = config.centerDrops;
        }

        AreaToolsConfig toConfig() {
            return new AreaToolsConfig(radius, wideRadius, deepDepth, netheriteMiningSpeed, durabilityDivisor, centerDrops);
        }
    }
}
