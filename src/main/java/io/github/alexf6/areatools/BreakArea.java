package io.github.alexf6.areatools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Lógica compartida entre servidor (romper) y cliente (contorno) para el área de la herramienta. */
public final class BreakArea {

    /**
     * Posiciones del área alrededor de {@code center}, sin incluirlo: un cuadrado perpendicular a la cara
     * golpeada que se extiende {@code shape.depth()} bloques hacia dentro. Cuesta O(volumen) y no depende del mundo.
     */
    public static List<BlockPos> neighbours(BlockPos center, Direction face, AreaShape shape) {
        int radius = shape.radius();
        int depthStep = -face.getAxisDirection().getStep(); // hacia dentro del bloque golpeado
        List<BlockPos> result = new ArrayList<>(shape.volume() - 1);

        for (int d = 0; d < shape.depth(); d++) {
            for (int a = -radius; a <= radius; a++) {
                for (int b = -radius; b <= radius; b++) {
                    if (d == 0 && a == 0 && b == 0) continue;
                    result.add(switch (face.getAxis()) {
                        case X -> center.offset(d * depthStep, a, b);
                        case Y -> center.offset(a, d * depthStep, b);
                        case Z -> center.offset(a, b, d * depthStep);
                    });
                }
            }
        }
        return result;
    }

    /** Bloque vecino que la herramienta también romperá para {@code player}. */
    public static boolean canBreak(Player player, Level level, BlockPos pos, ItemStack tool, ToolKind kind) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !kind.isMineable(state)) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false; // bedrock, etc.
        if (!tool.isCorrectToolForDrops(state)) return false;
        // Protección de spawn / claims y modos aventura y espectador
        return level.mayInteract(player, pos) && !player.blockActionRestricted(level, pos, player.gameMode());
    }

    private BreakArea() {}
}
