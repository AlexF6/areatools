package io.github.alexf6.areatools.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class AreaToolsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LevelRenderEvents.COLLECT_SUBMITS.register(new AreaOutlineRenderer()::render);
    }
}
