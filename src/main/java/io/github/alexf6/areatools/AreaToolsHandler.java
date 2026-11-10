package io.github.alexf6.areatools;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Conecta los eventos de Fabric con {@link AreaBreaker}. */
public final class AreaToolsHandler {

    public static void register() {
        AreaBreaker breaker = new AreaBreaker();
        AreaToolsConfig config = AreaToolsConfig.get();

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> breaker.onEntityLoad(entity));

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return true;
            if (breaker.isActive()) return true;
            if (player.isShiftKeyDown()) return true; // agachado = rompe solo 1 bloque

            ItemStack tool = player.getMainHandItem();
            ToolKind kind = ToolKind.of(tool);
            if (kind == null || !kind.isMineable(state)) return true;

            // Cara del bloque que el jugador está mirando
            HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
            if (!(hit instanceof BlockHitResult blockHit)) return true;
            Direction face = blockHit.getDirection();

            breaker.breakArea(serverPlayer, level, pos, face, tool, kind, config);
            return true; // permitir que se rompa el bloque central
        });
    }

    private AreaToolsHandler() {}
}
