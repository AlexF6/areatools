package io.github.alexf6.areatools;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/** Agachado + clic derecho con una herramienta de área alterna su modo. */
public final class ModeToggle {

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) return InteractionResult.PASS;

            ItemStack tool = player.getMainHandItem();
            if (ToolKind.of(tool) == null) return InteractionResult.PASS;

            // El cliente solo anima el gesto; el servidor cambia el modo y el ítem se sincroniza solo.
            if (!level.isClientSide()) {
                AreaMode next = tool.getOrDefault(ModComponents.AREA_MODE, AreaMode.FLAT).next();
                tool.set(ModComponents.AREA_MODE, next);
                player.sendOverlayMessage(next.displayName());
                player.playSound(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 0.6F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        });
    }

    private ModeToggle() {}
}
