package io.github.alexf6.areatools;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Conecta los eventos de Fabric con {@link AreaBreaker}.
 *
 * <p>La cara se mide en {@code BEFORE}, cuando el bloque aún existe, pero los vecinos se rompen en {@code AFTER}:
 * así solo se rompen si el bloque central se rompió de verdad. Si otro mod lo veta en {@code BEFORE} (o el juego lo
 * impide), no se rompe nada del área.
 */
public final class AreaToolsHandler {

    /** Lo que {@code BEFORE} anotó para que {@code AFTER} rompa el área: quién, qué bloque y por qué cara. */
    private record PendingBreak(ServerPlayer player, BlockPos pos, Direction face) {}

    // Los eventos de un mismo golpe son consecutivos en el hilo del servidor; si otro listener veta el golpe no
    // hay AFTER y queda un resto, que el siguiente BEFORE sustituye y AFTER descarta si no coincide jugador y bloque
    private static @Nullable PendingBreak pending;

    public static void register() {
        AreaBreaker breaker = new AreaBreaker();
        AreaToolsConfig config = AreaToolsConfig.get();

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> breaker.onEntityLoad(entity));

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            pending = null;
            if (!(player instanceof ServerPlayer serverPlayer)) return true;
            if (breaker.isActive()) return true; // es uno de los vecinos: no vuelve a romper un área
            if (player.isShiftKeyDown()) return true; // agachado = rompe solo 1 bloque

            ToolKind kind = ToolKind.of(player.getMainHandItem());
            if (kind == null || !kind.isMineable(state)) return true;

            // Cara del bloque que el jugador está mirando; debe ser el mismo bloque que se está rompiendo
            HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
            if (!(hit instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(pos)) return true;

            pending = new PendingBreak(serverPlayer, pos.immutable(), blockHit.getDirection());
            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            PendingBreak mine = pending;
            pending = null;
            if (mine == null || mine.player() != player || !mine.pos().equals(pos)) return;

            // La herramienta puede haberse roto al minar el bloque central
            ItemStack tool = player.getMainHandItem();
            ToolKind kind = ToolKind.of(tool);
            if (kind == null) return;

            breaker.breakArea(mine.player(), level, pos, mine.face(), tool, kind, config);
        });
    }

    private AreaToolsHandler() {}
}
