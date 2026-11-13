package io.github.alexf6.areatools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Adaptador a Minecraft, compartido entre servidor (romper) y cliente (contorno): convierte la geometría pura de
 * {@link AreaGeometry} en posiciones del mundo y decide qué bloque vecino puede romper un jugador.
 */
public final class BreakArea {

    /**
     * Posiciones del área alrededor de {@code center}, sin incluirlo; la forma sale de {@link AreaGeometry}.
     * Cuesta O(volumen) y no depende del mundo.
     */
    public static List<BlockPos> neighbours(BlockPos center, Direction face, AreaShape shape) {
        AreaGeometry.Axis axis = switch (face.getAxis()) {
            case X -> AreaGeometry.Axis.X;
            case Y -> AreaGeometry.Axis.Y;
            case Z -> AreaGeometry.Axis.Z;
        };
        List<AreaGeometry.Offset> offsets = AreaGeometry.neighbours(axis, face.getAxisDirection().getStep(), shape);
        List<BlockPos> result = new ArrayList<>(offsets.size());
        for (AreaGeometry.Offset o : offsets) result.add(center.offset(o.dx(), o.dy(), o.dz()));
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
