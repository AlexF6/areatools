package io.github.alexf6.areatools.client;

import java.util.List;

import io.github.alexf6.areatools.AreaMode;
import io.github.alexf6.areatools.AreaShape;
import io.github.alexf6.areatools.AreaToolsConfig;
import io.github.alexf6.areatools.BreakArea;
import io.github.alexf6.areatools.ModComponents;
import io.github.alexf6.areatools.ToolKind;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Dibuja el contorno de los bloques vecinos que la herramienta romperá; el central lo dibuja el juego. */
final class AreaOutlineRenderer {

    private static final int COLOR = ARGB.colorFromFloat(0.4F, 0.0F, 0.0F, 0.0F); // mismo estilo que el contorno de vanilla

    private final AreaToolsConfig config = AreaToolsConfig.get();

    // Las posiciones solo dependen de dónde se apunta y del tamaño: se recalculan al cambiar, no en cada frame.
    private BlockPos cachedCenter;
    private Direction cachedFace;
    private AreaShape cachedShape;
    private List<BlockPos> cachedArea = List.of();

    void render(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        if (player.isShiftKeyDown()) return; // agachado = 1 bloque, igual que en el servidor

        ItemStack tool = player.getMainHandItem();
        ToolKind kind = ToolKind.of(tool);
        if (kind == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos center = hit.getBlockPos();
        if (!kind.isMineable(mc.level.getBlockState(center))) return;

        AreaShape shape = AreaShape.of(tool.getOrDefault(ModComponents.AREA_MODE, AreaMode.FLAT), config);
        List<BlockPos> area = areaFor(center, hit.getDirection(), shape);

        Vec3 camera = context.levelState().cameraRenderState.pos;
        float lineWidth = context.gameRenderer().gameRenderState().windowRenderState.appropriateLineWidth;
        var poseStack = context.poseStack();

        for (BlockPos target : area) {
            if (!BreakArea.canBreak(player, mc.level, target, tool, kind)) continue;

            poseStack.pushPose();
            poseStack.translate(target.getX() - camera.x, target.getY() - camera.y, target.getZ() - camera.z);
            context.submitNodeCollector().submitShapeOutline(
                    poseStack, mc.level.getBlockState(target).getShape(mc.level, target),
                    RenderTypes.linesTranslucent(), COLOR, lineWidth, true);
            poseStack.popPose();
        }
    }

    private List<BlockPos> areaFor(BlockPos center, Direction face, AreaShape shape) {
        if (!center.equals(cachedCenter) || face != cachedFace || !shape.equals(cachedShape)) {
            cachedCenter = center;
            cachedFace = face;
            cachedShape = shape;
            cachedArea = BreakArea.neighbours(center, face, shape);
        }
        return cachedArea;
    }
}
