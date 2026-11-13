package io.github.alexf6.areatools;

import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

/** Modo de la herramienta: FLAT rompe un cuadrado, WIDE uno más grande y DEEP además hacia dentro del bloque. */
public enum AreaMode implements StringRepresentable, TooltipProvider {
    FLAT("flat"),
    WIDE("wide"),
    DEEP("deep");

    private static final AreaMode[] VALUES = values();

    public static final Codec<AreaMode> CODEC = StringRepresentable.fromEnum(() -> VALUES);
    public static final StreamCodec<ByteBuf, AreaMode> STREAM_CODEC =
            ByteBufCodecs.idMapper(id -> VALUES[id], AreaMode::ordinal);

    private final String name;

    AreaMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Tamaño del área de este modo con los valores de {@code config}. */
    public AreaShape shape(AreaToolsConfig config) {
        return switch (this) {
            case FLAT -> new AreaShape(config.radius(), 1);
            case WIDE -> new AreaShape(config.wideRadius(), 1);
            case DEEP -> new AreaShape(config.radius(), config.deepDepth());
        };
    }

    public AreaMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public Component displayName() {
        return Component.translatable("areatools.mode." + name);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag,
                             DataComponentGetter components) {
        consumer.accept(Component.translatable("areatools.tooltip.mode", displayName()).withStyle(ChatFormatting.GRAY));
    }
}
