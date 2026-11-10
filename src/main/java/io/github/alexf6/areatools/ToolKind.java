package io.github.alexf6.areatools;

import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Tipo de herramienta de área: qué bloques mina y de qué tag de ítems se reconoce. */
public enum ToolKind {
    HAMMER("hammer", BlockTags.MINEABLE_WITH_PICKAXE, (props, material) -> props.pickaxe(material, 1.0F, -3.2F)),
    EXCAVATOR("excavator", BlockTags.MINEABLE_WITH_SHOVEL, (props, material) -> props.shovel(material, 1.5F, -3.2F));

    private static final ToolKind[] VALUES = values();

    private final String suffix;
    private final TagKey<Item> itemTag;
    private final TagKey<Block> mineableBlocks;
    private final BiFunction<Item.Properties, ToolMaterial, Item.Properties> toolProperties;

    ToolKind(String suffix, TagKey<Block> mineableBlocks,
             BiFunction<Item.Properties, ToolMaterial, Item.Properties> toolProperties) {
        this.suffix = suffix;
        // areatools:hammers / areatools:excavators
        this.itemTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AreaToolsMod.MOD_ID, suffix + "s"));
        this.mineableBlocks = mineableBlocks;
        this.toolProperties = toolProperties;
    }

    /** Sufijo del id del ítem: {@code <material>_<suffix>}. */
    public String suffix() {
        return suffix;
    }

    public TagKey<Item> itemTag() {
        return itemTag;
    }

    /** Aplica a las propiedades el comportamiento de herramienta (pico o pala) del material dado. */
    public Item.Properties applyTo(Item.Properties props, ToolMaterial material) {
        return toolProperties.apply(props, material);
    }

    public boolean isMineable(BlockState state) {
        return state.is(mineableBlocks);
    }

    public boolean matches(ItemStack stack) {
        return stack.is(itemTag);
    }

    /** Tipo de herramienta de área del ítem, o {@code null} si no es ninguna. */
    public static @Nullable ToolKind of(ItemStack stack) {
        for (ToolKind kind : VALUES) {
            if (kind.matches(stack)) return kind;
        }
        return null;
    }
}
