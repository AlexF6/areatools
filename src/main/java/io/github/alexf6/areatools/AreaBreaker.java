package io.github.alexf6.areatools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Rompe el área de una herramienta (lado servidor). Solo se usa desde el hilo del servidor y sin reentrada
 * entre jugadores, por eso el estado es un simple campo y no un conjunto por jugador.
 */
public final class AreaBreaker {

    private final List<ItemEntity> spawnedDrops = new ArrayList<>();
    private Level currentLevel;
    private boolean active;

    /** Verdadero mientras se rompen los vecinos: cada uno vuelve a disparar el evento de romper bloque. */
    public boolean isActive() {
        return active;
    }

    /** Rompe los vecinos de {@code center}; el central lo rompe el juego a continuación. */
    public void breakArea(ServerPlayer player, Level level, BlockPos center, Direction face, ItemStack tool,
                          ToolKind kind, AreaToolsConfig config) {
        AreaMode mode = tool.getOrDefault(ModComponents.AREA_MODE, AreaMode.FLAT);
        AreaShape shape = mode.shape(config);

        active = true;
        currentLevel = level;
        try {
            int broken = 0;
            for (BlockPos target : BreakArea.neighbours(center, face, shape)) {
                if (!BreakArea.canBreak(player, level, target, tool, kind)) continue;

                int damageBefore = tool.getDamageValue();
                // Rompe como si lo hiciera el jugador: drops, durabilidad, encantamientos
                if (!player.gameMode.destroyBlock(target)) continue;
                if (tool.isEmpty()) break; // la herramienta se rompió

                broken++;
                if (broken % config.durabilityDivisor() != 0) {
                    tool.setDamageValue(damageBefore); // solo 1 de cada N bloques gasta durabilidad
                }
            }

            if (config.centerDrops()) {
                gatherDrops(center, Math.max(shape.radius(), shape.depth()) + 1.5);
            }
        } finally {
            active = false;
            currentLevel = null;
            spawnedDrops.clear();
        }
    }

    /** Anota los ítems que aparecen mientras se rompe el área, filtrando por mundo. */
    public void onEntityLoad(Entity entity) {
        if (active && entity instanceof ItemEntity item && entity.level() == currentLevel) {
            spawnedDrops.add(item);
        }
    }

    /**
     * Junta los drops en el bloque central si están cerca del área minada. Aún no ha pasado ningún tick,
     * así que ese bloque ya estará roto cuando los ítems se muevan por primera vez.
     */
    private void gatherDrops(BlockPos center, double maxDistance) {
        Vec3 middle = Vec3.atCenterOf(center);
        double maxDistSq = maxDistance * maxDistance;
        for (ItemEntity item : spawnedDrops) {
            if (item.isAlive() && item.position().distanceToSqr(middle) <= maxDistSq) {
                item.setPos(middle.x, middle.y - item.getBbHeight() / 2.0, middle.z);
            }
        }
    }
}
