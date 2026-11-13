package io.github.alexf6.areatools;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    private static final float DEFAULT_NETHERITE_MINING_SPEED = 70.0F;

    public static final AreaToolsConfig DEFAULTS = new AreaToolsConfig(1, 2, 3, DEFAULT_NETHERITE_MINING_SPEED, 2, true);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public AreaToolsConfig {
        radius = clamp("radius", radius, 1, MAX_RADIUS);
        wideRadius = clamp("wideRadius", wideRadius, 1, MAX_RADIUS);
        deepDepth = clamp("deepDepth", deepDepth, 1, MAX_DEPTH);
        netheriteMiningSpeed = clampSpeed(netheriteMiningSpeed);
        durabilityDivisor = clamp("durabilityDivisor", durabilityDivisor, 1, MAX_DIVISOR);
    }

    /** Corrige {@code value} al rango {@code [min, max]} y lo avisa en el log. */
    private static int clamp(String key, int value, int min, int max) {
        int clamped = Math.clamp(value, min, max);
        if (clamped != value) {
            AreaToolsMod.LOGGER.warn("Valor de '{}' fuera de rango ({}); ajustado a {}.", key, value, clamped);
        }
        return clamped;
    }

    /** NaN vuelve al valor por defecto (no tiene límite al que acercarse); el resto se acota a [1, MAX_SPEED]. */
    private static float clampSpeed(float speed) {
        if (Float.isNaN(speed)) {
            AreaToolsMod.LOGGER.warn("Valor de 'netheriteMiningSpeed' no es un número (NaN); ajustado al valor por defecto ({}).",
                    DEFAULT_NETHERITE_MINING_SPEED);
            return DEFAULT_NETHERITE_MINING_SPEED;
        }
        float clamped = Math.clamp(speed, 1.0F, MAX_SPEED);
        if (clamped != speed) {
            AreaToolsMod.LOGGER.warn("Valor de 'netheriteMiningSpeed' fuera de rango ({}); ajustado a {}.", speed, clamped);
        }
        return clamped;
    }

    /** Se carga una sola vez, la primera vez que se pide (antes de registrar los ítems). */
    public static AreaToolsConfig get() {
        return Holder.INSTANCE;
    }

    /**
     * Lee la configuración de {@code file}. Si no existe la crea con los valores por defecto; si está rota
     * (JSON inválido, un valor de tipo incorrecto o bytes que no son UTF-8) la respalda como
     * {@code areatools.json.bak}, sustituyendo un respaldo anterior, y regenera una nueva con los valores por
     * defecto. Un archivo vacío o {@code {}} usa los valores por defecto sin tocarlo.
     */
    static AreaToolsConfig load(Path file) {
        if (!Files.exists(file)) {
            write(file, DEFAULTS);
            return DEFAULTS;
        }
        try {
            // Se lee el texto antes de parsearlo: Gson convierte un fallo de lectura en JsonParseException, y un
            // archivo que solo no se pudo leer no debe tratarse como roto (ni respaldarse ni sobrescribirse)
            Raw raw = GSON.fromJson(Files.readString(file), Raw.class);
            return raw == null ? DEFAULTS : raw.toConfig();
        } catch (JsonParseException | CharacterCodingException e) {
            // Un valor del tipo equivocado, un JSON roto o bytes que no son UTF-8: el archivo está dañado
            backUpAndRegenerate(file, e);
            return DEFAULTS;
        } catch (IOException e) {
            AreaToolsMod.LOGGER.warn("No se pudo leer {}; se usan los valores por defecto sin modificarlo.", file, e);
            return DEFAULTS;
        }
    }

    private static void backUpAndRegenerate(Path file, Exception cause) {
        Path backup = file.resolveSibling(file.getFileName() + ".bak");
        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            AreaToolsMod.LOGGER.warn("{} no es válido ({}); se guardó como {} y se regeneró con los valores por defecto.",
                    file, cause.getMessage(), backup);
        } catch (IOException e) {
            AreaToolsMod.LOGGER.warn("No se pudo respaldar {} a {}: {}; se usan los valores por defecto sin sobrescribir.",
                    file, backup, e.toString());
            return;
        }
        write(file, DEFAULTS);
    }

    private static void write(Path file, AreaToolsConfig config) {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(new Raw(config), writer);
            }
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
            this.radius = config.radius;
            this.wideRadius = config.wideRadius;
            this.deepDepth = config.deepDepth;
            this.netheriteMiningSpeed = config.netheriteMiningSpeed;
            this.durabilityDivisor = config.durabilityDivisor;
            this.centerDrops = config.centerDrops;
        }

        AreaToolsConfig toConfig() {
            return new AreaToolsConfig(radius, wideRadius, deepDepth, netheriteMiningSpeed, durabilityDivisor,
                    centerDrops);
        }
    }
}
