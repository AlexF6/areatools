package io.github.alexf6.areatools.client;

import io.github.alexf6.areatools.AreaToolsConfig;
import io.github.alexf6.areatools.network.AreaConfigSyncPayload;
import io.github.alexf6.areatools.network.SyncedAreaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class AreaToolsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LevelRenderEvents.COLLECT_SUBMITS.register(new AreaOutlineRenderer()::render);

        // El tamaño del área lo dicta el servidor al entrar y se descarta al salir
        ClientPlayNetworking.registerGlobalReceiver(AreaConfigSyncPayload.TYPE,
                (payload, context) -> SyncedAreaConfig.set(payload.applyTo(AreaToolsConfig.get())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SyncedAreaConfig.clear());
    }
}
