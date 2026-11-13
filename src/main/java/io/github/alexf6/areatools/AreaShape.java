package io.github.alexf6.areatools;

/**
 * Tamaño del área que rompe la herramienta, en bloques. Lógica pura: sin dependencias de Minecraft.
 *
 * @param radius mitad del lado del cuadrado perpendicular a la cara golpeada (0 = solo el bloque golpeado)
 * @param depth  bloques hacia dentro, contando el golpeado (1 = solo el cuadrado)
 */
public record AreaShape(int radius, int depth) {

    public AreaShape {
        if (radius < 0) throw new IllegalArgumentException("radius no puede ser negativo: " + radius);
        if (depth < 1) throw new IllegalArgumentException("depth debe ser al menos 1: " + depth);
    }

    /**
     * Total de bloques del área, incluido el golpeado: {@code (2 * radius + 1)^2 * depth}. Con los máximos de la
     * configuración (radio 3, profundidad 5) son 245 bloques por golpe. Es O(1).
     */
    public int volume() {
        int side = 2 * radius + 1;
        return side * side * depth;
    }
}
