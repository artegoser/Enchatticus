package ru.artegoser.enchatticus.permission;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import ru.artegoser.enchatticus.config.EnchatticusConfig;
import ru.artegoser.enchatticus.integration.LuckPermsBridge;

public final class PermissionService {
    private PermissionService() {
    }

    public static boolean canUseLocal(ServerPlayer player, EnchatticusConfig config) {
        return LuckPermsBridge.hasPermission(player, config.permissions.localChat, config.permissions.localChatDefault);
    }

    public static boolean canUseGlobal(ServerPlayer player, EnchatticusConfig config) {
        return LuckPermsBridge.hasPermission(player, config.permissions.globalChat, config.permissions.globalChatDefault);
    }

    public static boolean canUseMe(ServerPlayer player, EnchatticusConfig config) {
        return LuckPermsBridge.hasPermission(player, config.permissions.me, config.permissions.meDefault);
    }

    public static boolean canReload(CommandSourceStack source, EnchatticusConfig config) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return true;
        }

        boolean vanillaDefault = source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
        return LuckPermsBridge.hasPermission(player, config.permissions.reload, vanillaDefault);
    }
}
