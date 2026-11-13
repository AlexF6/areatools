package io.github.alexf6.areatools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AreaShapeTest {

    @Test
    void volumeCountsTheSquareTimesTheDepthIncludingTheHitBlock() {
        assertEquals(1, new AreaShape(0, 1).volume());
        assertEquals(9, new AreaShape(1, 1).volume());
        assertEquals(27, new AreaShape(1, 3).volume());
        assertEquals(245, new AreaShape(3, 5).volume());
    }

    @Test
    void impossibleShapesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AreaShape(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new AreaShape(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new AreaShape(1, -4));
    }
}
