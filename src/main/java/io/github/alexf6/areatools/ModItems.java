package io.github.alexf6.areatools;

import java.util.List;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class ModItems {

    // Netherita con la velocidad de la configuración: por defecto la piedra sale al instante y las menas
    // necesitan Eficiencia, así que el encantamiento sigue importando.
    private static final ToolMaterial NETHERITE_FAST = new ToolMaterial(
            ToolMaterial.NETHERITE.incorrectBlocksForDrops(),
            ToolMaterial.NETHERITE.durability(),
            AreaToolsConfig.get().netheriteMiningSpeed(),
            ToolMaterial.NETHERITE.attackDamageBonus(),
            ToolMaterial.NETHERITE.enchantmentValue(),
            ToolMaterial.NETHERITE.repairItems()
    );

    // Cada uno funciona como la herramienta de su material, pero más lento al golpear (-3.2F).
    public static final Item IRON_HAMMER = register("iron", ToolKind.HAMMER, ToolMaterial.IRON, false);
    public static final Item COPPER_HAMMER = register("copper", ToolKind.HAMMER, ToolMaterial.COPPER, false);
    public static final Item GOLDEN_HAMMER = register("golden", ToolKind.HAMMER, ToolMaterial.GOLD, false);
    public static final Item DIAMOND_HAMMER = register("diamond", ToolKind.HAMMER, ToolMaterial.DIAMOND, false);
    public static final Item NETHERITE_HAMMER = register("netherite", ToolKind.HAMMER, NETHERITE_FAST, true);

    public static final Item IRON_EXCAVATOR = register("iron", ToolKind.EXCAVATOR, ToolMaterial.IRON, false);
    public static final Item COPPER_EXCAVATOR = register("copper", ToolKind.EXCAVATOR, ToolMaterial.COPPER, false);
    public static final Item GOLDEN_EXCAVATOR = register("golden", ToolKind.EXCAVATOR, ToolMaterial.GOLD, false);
    public static final Item DIAMOND_EXCAVATOR = register("diamond", ToolKind.EXCAVATOR, ToolMaterial.DIAMOND, false);
    public static final Item NETHERITE_EXCAVATOR = register("netherite", ToolKind.EXCAVATOR, NETHERITE_FAST, true);

    private static final List<Item> CREATIVE_TAB_ITEMS = List.of(
            COPPER_HAMMER, IRON_HAMMER, GOLDEN_HAMMER, DIAMOND_HAMMER, NETHERITE_HAMMER,
            COPPER_EXCAVATOR, IRON_EXCAVATOR, GOLDEN_EXCAVATOR, DIAMOND_EXCAVATOR, NETHERITE_EXCAVATOR);

    private static Item register(String material, ToolKind kind, ToolMaterial toolMaterial, boolean fireResistant) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(AreaToolsMod.MOD_ID, material + "_" + kind.suffix())
        );
        Item.Properties props = kind.applyTo(new Item.Properties(), toolMaterial)
                .component(ModComponents.AREA_MODE, AreaMode.FLAT);
        if (fireResistant) props = props.fireResistant();
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(props.setId(key)));
    }

    public static void init() {
        // Mostrar las herramientas en la pestaña "Herramientas y utilidades"
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(tab -> CREATIVE_TAB_ITEMS.forEach(tab::accept));
    }

    private ModItems() {}
}
