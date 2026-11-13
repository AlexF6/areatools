package io.github.alexf6.areatools;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometría del área de la herramienta, sin dependencias de Minecraft: trabaja con desplazamientos enteros (en
 * bloques) respecto al bloque golpeado, y {@link BreakArea} los convierte en posiciones del mundo.
 */
public final class AreaGeometry {

    /** Eje perpendicular a la cara golpeada. */
    public enum Axis { X, Y, Z }

    /** Desplazamiento en bloques respecto al bloque golpeado. */
    public record Offset(int dx, int dy, int dz) {}

    /**
     * Desplazamientos del área alrededor del bloque golpeado, sin incluirlo: un cuadrado perpendicular a la cara
     * que se extiende {@code shape.depth()} bloques hacia dentro.
     *
     * <p>Coste: O(volumen) tiempo y memoria, como máximo 244 elementos con los topes de la configuración
     * (radio 3 x profundidad 5); no depende del mundo.
     *
     * @param faceAxis eje de la cara golpeada
     * @param faceStep +1 si la cara mira hacia el sentido positivo del eje y -1 si mira al negativo
     */
    public static List<Offset> neighbours(Axis faceAxis, int faceStep, AreaShape shape) {
        if (faceStep != 1 && faceStep != -1) throw new IllegalArgumentException("faceStep debe ser 1 o -1: " + faceStep);
        int radius = shape.radius();
        int depthStep = -faceStep; // hacia dentro del bloque golpeado
        List<Offset> result = new ArrayList<>(shape.volume() - 1);

        for (int d = 0; d < shape.depth(); d++) {
            for (int a = -radius; a <= radius; a++) {
                for (int b = -radius; b <= radius; b++) {
                    if (d == 0 && a == 0 && b == 0) continue;
                    result.add(switch (faceAxis) {
                        case X -> new Offset(d * depthStep, a, b);
                        case Y -> new Offset(a, d * depthStep, b);
                        case Z -> new Offset(a, b, d * depthStep);
                    });
                }
            }
        }
        return result;
    }

    private AreaGeometry() {}
}
