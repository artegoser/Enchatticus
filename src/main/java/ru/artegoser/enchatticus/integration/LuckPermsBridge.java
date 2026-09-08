package ru.artegoser.enchatticus.integration;

import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;
import ru.artegoser.enchatticus.Enchatticus;
import ru.artegoser.enchatticus.config.EnchatticusConfig;

public final class LuckPermsBridge {
    private static boolean warnedUnavailable;

    private LuckPermsBridge() {
    }

    public static Meta meta(ServerPlayer player, EnchatticusConfig config) {
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return Meta.EMPTY;
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) {
                return Meta.EMPTY;
            }

            var meta = user.getCachedData().getMetaData();
            String prefix = config.luckPerms.usePrefix ? nullToEmpty(meta.getPrefix()) : "";
            String suffix = config.luckPerms.useSuffix ? nullToEmpty(meta.getSuffix()) : "";
            return new Meta(prefix, suffix);
        } catch (Throwable throwable) {
            if (!warnedUnavailable) {
                warnedUnavailable = true;
                Enchatticus.LOGGER.warn("LuckPerms is installed but its API is not available to Enchatticus. Prefixes will be omitted.", throwable);
            }
            return Meta.EMPTY;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record Meta(String prefix, String suffix) {
        public static final Meta EMPTY = new Meta("", "");
    }
}
