package ru.artegoser.enchatticus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import ru.artegoser.enchatticus.Enchatticus;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path = FabricLoader.getInstance().getConfigDir().resolve("enchatticus.json");

    private volatile EnchatticusConfig config = new EnchatticusConfig();

    public EnchatticusConfig get() {
        return config;
    }

    public synchronized boolean load() {
        try {
            Files.createDirectories(path.getParent());

            if (!Files.exists(path)) {
                config = new EnchatticusConfig();
                save();
                Enchatticus.LOGGER.info("Created default config at {}", path);
                return true;
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                EnchatticusConfig loaded = GSON.fromJson(reader, EnchatticusConfig.class);
                config = sanitize(loaded == null ? new EnchatticusConfig() : loaded);
            }

            Enchatticus.LOGGER.info("Loaded config from {}", path);
            return true;
        } catch (Exception e) {
            Enchatticus.LOGGER.error("Failed to load {}. Keeping the previous configuration.", path, e);
            return false;
        }
    }

    public synchronized void save() throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(config, writer);
        }
    }

    public Path path() {
        return path;
    }

    private static EnchatticusConfig sanitize(EnchatticusConfig value) {
        if (value.chat == null) value.chat = new EnchatticusConfig.Chat();
        if (value.luckPerms == null) value.luckPerms = new EnchatticusConfig.LuckPerms();

        EnchatticusConfig.Chat defaults = new EnchatticusConfig.Chat();
        if (value.chat.localRadius < 0.0 || !Double.isFinite(value.chat.localRadius)) {
            value.chat.localRadius = defaults.localRadius;
        }
        if (value.chat.globalTrigger == null || value.chat.globalTrigger.isEmpty()) {
            value.chat.globalTrigger = defaults.globalTrigger;
        }
        if (value.chat.localPrefix == null) value.chat.localPrefix = defaults.localPrefix;
        if (value.chat.globalPrefix == null) value.chat.globalPrefix = defaults.globalPrefix;
        if (value.chat.localFormat == null) value.chat.localFormat = defaults.localFormat;
        if (value.chat.globalFormat == null) value.chat.globalFormat = defaults.globalFormat;
        if (value.chat.localMeFormat == null) value.chat.localMeFormat = defaults.localMeFormat;
        if (value.chat.globalMeFormat == null) value.chat.globalMeFormat = defaults.globalMeFormat;
        return value;
    }
}
