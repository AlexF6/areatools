package io.github.alexf6.areatools;

import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModComponents {

    /** Modo de la herramienta. Se guarda en el ítem, así que viaja solo al cliente y sobrevive al guardar. */
    public static final DataComponentType<AreaMode> AREA_MODE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(AreaToolsMod.MOD_ID, "mode"),
            DataComponentType.<AreaMode>builder()
                    .persistent(AreaMode.CODEC)
                    .networkSynchronized(AreaMode.STREAM_CODEC)
                    .build()
    );

    public static void init() {
        ItemComponentTooltipProviderRegistry.addLast(AREA_MODE);
    }

    private ModComponents() {}
}
