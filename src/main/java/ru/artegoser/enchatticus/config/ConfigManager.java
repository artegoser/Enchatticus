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
import java.util.ArrayList;

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
        EnchatticusConfig defaults = new EnchatticusConfig();
        if (value.chat == null) value.chat = defaults.chat;
        if (value.tab == null) value.tab = defaults.tab;
        if (value.luckPerms == null) value.luckPerms = defaults.luckPerms;
        if (value.permissions == null) value.permissions = defaults.permissions;
        if (value.badges == null) value.badges = new ArrayList<>();

        if (value.chat.localRadius < 0.0 || !Double.isFinite(value.chat.localRadius)) value.chat.localRadius = defaults.chat.localRadius;
        if (value.chat.globalTrigger == null || value.chat.globalTrigger.isEmpty()) value.chat.globalTrigger = defaults.chat.globalTrigger;
        if (value.chat.localPrefix == null) value.chat.localPrefix = defaults.chat.localPrefix;
        if (value.chat.globalPrefix == null) value.chat.globalPrefix = defaults.chat.globalPrefix;
        if (value.chat.localFormat == null) value.chat.localFormat = defaults.chat.localFormat;
        if (value.chat.globalFormat == null) value.chat.globalFormat = defaults.chat.globalFormat;
        if (value.chat.localMeFormat == null) value.chat.localMeFormat = defaults.chat.localMeFormat;
        if (value.chat.globalMeFormat == null) value.chat.globalMeFormat = defaults.chat.globalMeFormat;

        // Migrate the exact pre-badge default formats without touching custom formats.
        if (value.chat.localFormat.equals("{channel}{prefix}{name}{suffix}&7: &f{message}")) value.chat.localFormat = defaults.chat.localFormat;
        if (value.chat.globalFormat.equals("{channel}{prefix}{name}{suffix}&7: &f{message}")) value.chat.globalFormat = defaults.chat.globalFormat;
        if (value.chat.localMeFormat.equals("{channel}* {prefix}{name}{suffix} &7{message}")) value.chat.localMeFormat = defaults.chat.localMeFormat;
        if (value.chat.globalMeFormat.equals("{channel}* {prefix}{name}{suffix} &f{message}")) value.chat.globalMeFormat = defaults.chat.globalMeFormat;

        if (value.tab.format == null) value.tab.format = defaults.tab.format;

        if (value.permissions.localChat == null || value.permissions.localChat.isBlank()) value.permissions.localChat = defaults.permissions.localChat;
        if (value.permissions.globalChat == null || value.permissions.globalChat.isBlank()) value.permissions.globalChat = defaults.permissions.globalChat;
        if (value.permissions.me == null || value.permissions.me.isBlank()) value.permissions.me = defaults.permissions.me;
        if (value.permissions.reload == null || value.permissions.reload.isBlank()) value.permissions.reload = defaults.permissions.reload;

        value.badges.removeIf(badge -> badge == null);
        for (EnchatticusConfig.Badge badge : value.badges) {
            if (badge.id == null) badge.id = "badge";
            if (badge.text == null) badge.text = "";
            if (badge.hover == null) badge.hover = "";
            if (badge.group == null) badge.group = "";
            if (badge.permission == null) badge.permission = "";
            if (badge.clickCommand == null) badge.clickCommand = "";
            if (badge.clickAction == null) badge.clickAction = "suggest";
        }
        return value;
    }
}
