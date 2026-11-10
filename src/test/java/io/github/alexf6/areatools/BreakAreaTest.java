package io.github.alexf6.areatools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

class BreakAreaTest {

    private static final BlockPos CENTER = new BlockPos(10, 64, -5);
    private static final AreaShape FLAT = new AreaShape(1, 1);

    @ParameterizedTest
    @EnumSource(Direction.class)
    void flatAreaIsEightDistinctNeighboursWithoutTheCenter(Direction face) {
        List<BlockPos> area = BreakArea.neighbours(CENTER, face, FLAT);

        assertEquals(8, area.size());
        assertEquals(8, new HashSet<>(area).size(), "sin posiciones repetidas");
        assertFalse(area.contains(CENTER));
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void flatAreaIsPerpendicularToTheHitFace(Direction face) {
        for (BlockPos pos : BreakArea.neighbours(CENTER, face, FLAT)) {
            // No se mueve a lo largo del eje de la cara...
            assertEquals(CENTER.get(face.getAxis()), pos.get(face.getAxis()));
            // ...y queda dentro del cuadrado 3x3
            assertTrue(Math.abs(pos.getX() - CENTER.getX()) <= 1);
            assertTrue(Math.abs(pos.getY() - CENTER.getY()) <= 1);
            assertTrue(Math.abs(pos.getZ() - CENTER.getZ()) <= 1);
        }
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void deepAreaExtendsInwardOnly(Direction face) {
        AreaShape deep = new AreaShape(1, 3);
        List<BlockPos> area = BreakArea.neighbours(CENTER, face, deep);

        assertEquals(deep.volume() - 1, area.size());
        assertEquals(area.size(), new HashSet<>(area).size());
        for (BlockPos pos : area) {
            // Profundidad medida hacia dentro: 0 en la cara golpeada, 2 en el fondo
            int inward = -face.getAxisDirection().getStep() * (pos.get(face.getAxis()) - CENTER.get(face.getAxis()));
            assertTrue(inward >= 0 && inward <= 2, "fuera de rango: " + inward + " en " + face);
        }
        assertTrue(area.contains(CENTER.relative(face.getOpposite())), "incluye el bloque justo detrás");
        assertFalse(area.contains(CENTER.relative(face)), "no toca el aire delante de la cara");
    }

    @Test
    void largerRadiusGrowsTheSquare() {
        List<BlockPos> area = BreakArea.neighbours(CENTER, Direction.UP, new AreaShape(2, 1));

        assertEquals(24, area.size());
        assertTrue(area.contains(CENTER.offset(2, 0, -2)));
        assertFalse(area.contains(CENTER.offset(3, 0, 0)));
    }

    @Test
    void volumeMatchesTheNumberOfBlocksIncludingTheCenter() {
        assertEquals(9, FLAT.volume());
        assertEquals(27, new AreaShape(1, 3).volume());
        assertEquals(25, new AreaShape(2, 1).volume());
    }
}
