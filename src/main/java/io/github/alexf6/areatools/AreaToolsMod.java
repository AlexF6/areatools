package io.github.alexf6.areatools;

import io.github.alexf6.areatools.network.AreaConfigSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AreaToolsMod implements ModInitializer {
    public static final String MOD_ID = "areatools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModComponents.init();
        ModItems.init();
        AreaToolsHandler.register();
        ModeToggle.register();

        // El servidor manda su tamaño de área a cada cliente al entrar, para que el contorno coincida con lo que rompe
        PayloadTypeRegistry.clientboundPlay().register(AreaConfigSyncPayload.TYPE, AreaConfigSyncPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.getPlayer(), AreaConfigSyncPayload.TYPE)) {
                ServerPlayNetworking.send(handler.getPlayer(), AreaConfigSyncPayload.of(AreaToolsConfig.get()));
            }
        });
        LOGGER.info("AreaToolsMod inicializado correctamente.");
    }
}
