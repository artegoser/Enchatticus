package ru.artegoser.enchatticus;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.artegoser.enchatticus.chat.ChatService;
import ru.artegoser.enchatticus.config.ConfigManager;

public final class Enchatticus implements ModInitializer {
    public static final String MOD_ID = "enchatticus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ConfigManager configManager;

    @Override
    public void onInitialize() {
        configManager = new ConfigManager();
        configManager.load();

        ChatService chatService = new ChatService(configManager);
        chatService.register();

        LOGGER.info("Enchatticus initialized");
    }

    public static ConfigManager configManager() {
        return configManager;
    }
}
