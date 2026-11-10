package io.github.alexf6.areatools;

/**
 * Tamaño del área que rompe la herramienta.
 *
 * @param radius mitad del lado del cuadrado perpendicular a la cara golpeada
 * @param depth  bloques hacia dentro, contando el golpeado (1 = solo el cuadrado)
 */
public record AreaShape(int radius, int depth) {

    public static AreaShape of(AreaMode mode, AreaToolsConfig config) {
        return switch (mode) {
            case FLAT -> new AreaShape(config.radius(), 1);
            case WIDE -> new AreaShape(config.wideRadius(), 1);
            case DEEP -> new AreaShape(config.radius(), config.deepDepth());
        };
    }

    /** Total de bloques del área, incluido el golpeado. */
    public int volume() {
        int side = 2 * radius + 1;
        return side * side * depth;
    }
}
