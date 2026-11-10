package io.github.alexf6.areatools;

import net.fabricmc.api.ModInitializer;
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
        LOGGER.info("AreaToolsMod inicializado correctamente.");
    }
}
