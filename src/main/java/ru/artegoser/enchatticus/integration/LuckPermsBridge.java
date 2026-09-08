package ru.artegoser.enchatticus.integration;

import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ru.artegoser.enchatticus.Enchatticus;
import ru.artegoser.enchatticus.config.EnchatticusConfig;
import ru.artegoser.enchatticus.tab.TabService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LuckPermsBridge {
    private static boolean warnedUnavailable;
    private static final List<EventSubscription<?>> subscriptions = new ArrayList<>();

    private LuckPermsBridge() {
    }

    public static Meta meta(ServerPlayer player, EnchatticusConfig config) {
        User user = user(player);
        if (user == null) {
            return Meta.EMPTY;
        }

        try {
            var meta = user.getCachedData().getMetaData();
            String prefix = config.luckPerms.usePrefix ? nullToEmpty(meta.getPrefix()) : "";
            String suffix = config.luckPerms.useSuffix ? nullToEmpty(meta.getSuffix()) : "";
            return new Meta(prefix, suffix);
        } catch (Throwable throwable) {
            warnUnavailable(throwable);
            return Meta.EMPTY;
        }
    }

    public static boolean hasPermission(ServerPlayer player, String node, boolean defaultValue) {
        if (node == null || node.isBlank()) {
            return defaultValue;
        }

        User user = user(player);
        if (user == null) {
            return defaultValue;
        }

        try {
            Tristate result = user.getCachedData().getPermissionData().checkPermission(node.toLowerCase(Locale.ROOT));
            return result == Tristate.UNDEFINED ? defaultValue : result.asBoolean();
        } catch (Throwable throwable) {
            warnUnavailable(throwable);
            return defaultValue;
        }
    }

    public static boolean inheritsGroup(ServerPlayer player, String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return true;
        }

        User user = user(player);
        if (user == null) {
            return false;
        }

        try {
            return user.getInheritedGroups(user.getQueryOptions()).stream()
                .anyMatch(group -> group.getName().equalsIgnoreCase(groupName));
        } catch (Throwable throwable) {
            warnUnavailable(throwable);
            return false;
        }
    }

    public static synchronized void registerTabRefreshListeners(MinecraftServer server) {
        unregisterTabRefreshListeners();
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return;
        }

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            subscriptions.add(luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, event ->
                server.execute(() -> TabService.refreshPlayer(server, event.getUser().getUniqueId()))
            ));
            subscriptions.add(luckPerms.getEventBus().subscribe(GroupDataRecalculateEvent.class, event ->
                server.execute(() -> TabService.refreshAll(server))
            ));
        } catch (Throwable throwable) {
            warnUnavailable(throwable);
        }
    }

    public static synchronized void unregisterTabRefreshListeners() {
        for (EventSubscription<?> subscription : subscriptions) {
            subscription.close();
        }
        subscriptions.clear();
    }

    private static User user(ServerPlayer player) {
        if (!FabricLoader.getInstance().isModLoaded("luckperms")) {
            return null;
        }

        try {
            return LuckPermsProvider.get().getUserManager().getUser(player.getUUID());
        } catch (Throwable throwable) {
            warnUnavailable(throwable);
            return null;
        }
    }

    private static void warnUnavailable(Throwable throwable) {
        if (!warnedUnavailable) {
            warnedUnavailable = true;
            Enchatticus.LOGGER.warn("LuckPerms is installed but its API is not available to Enchatticus.", throwable);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record Meta(String prefix, String suffix) {
        public static final Meta EMPTY = new Meta("", "");
    }
}
