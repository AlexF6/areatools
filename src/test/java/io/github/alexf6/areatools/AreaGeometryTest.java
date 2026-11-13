package io.github.alexf6.areatools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.alexf6.areatools.AreaGeometry.Axis;
import io.github.alexf6.areatools.AreaGeometry.Offset;

/** Pruebas de la geometría pura: sin Minecraft, solo desplazamientos enteros. */
class AreaGeometryTest {

    private static int along(Offset o, Axis axis) {
        return switch (axis) {
            case X -> o.dx();
            case Y -> o.dy();
            case Z -> o.dz();
        };
    }

    @ParameterizedTest
    @EnumSource(Axis.class)
    void flatAreaIsEightDistinctNeighboursWithoutTheCenter(Axis axis) {
        List<Offset> area = AreaGeometry.neighbours(axis, 1, new AreaShape(1, 1));

        assertEquals(8, area.size());
        assertEquals(8, new HashSet<>(area).size(), "sin desplazamientos repetidos");
        assertFalse(area.contains(new Offset(0, 0, 0)));
        for (Offset o : area) assertEquals(0, along(o, axis), "perpendicular a la cara");
    }

    @ParameterizedTest
    @EnumSource(Axis.class)
    void deepAreaGoesInwardOnlyForBothFaceDirections(Axis axis) {
        for (int step : new int[] {1, -1}) {
            AreaShape deep = new AreaShape(1, 3);
            List<Offset> area = AreaGeometry.neighbours(axis, step, deep);

            assertEquals(deep.volume() - 1, area.size());
            assertEquals(area.size(), new HashSet<>(area).size());
            for (Offset o : area) {
                int inward = -step * along(o, axis);
                assertTrue(inward >= 0 && inward <= 2, "fuera de rango: " + inward);
            }
        }
    }

    @Test
    void radiusZeroIsOnlyTheHitBlockSoNoNeighbours() {
        assertTrue(AreaGeometry.neighbours(Axis.Y, 1, new AreaShape(0, 1)).isEmpty());
        assertEquals(2, AreaGeometry.neighbours(Axis.Y, 1, new AreaShape(0, 3)).size(), "solo hacia dentro");
    }

    @Test
    void largerRadiusGrowsTheSquare() {
        List<Offset> area = AreaGeometry.neighbours(Axis.Y, 1, new AreaShape(2, 1));

        assertEquals(24, area.size());
        assertTrue(area.contains(new Offset(2, 0, -2)));
        assertFalse(area.contains(new Offset(3, 0, 0)));
    }

    @Test
    void theLargestConfiguredAreaIs244NeighboursAndStaysBounded() {
        AreaShape max = new AreaShape(AreaToolsConfig.MAX_RADIUS, AreaToolsConfig.MAX_DEPTH);

        assertEquals(245, max.volume());
        assertEquals(244, AreaGeometry.neighbours(Axis.Z, -1, max).size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, -2, 5})
    void faceStepMustBePlusOrMinusOne(int step) {
        assertThrows(IllegalArgumentException.class, () -> AreaGeometry.neighbours(Axis.X, step, new AreaShape(1, 1)));
    }
}
