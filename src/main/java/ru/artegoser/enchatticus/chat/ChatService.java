package ru.artegoser.enchatticus.chat;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ru.artegoser.enchatticus.Enchatticus;
import ru.artegoser.enchatticus.badge.BadgeService;
import ru.artegoser.enchatticus.config.ConfigManager;
import ru.artegoser.enchatticus.config.EnchatticusConfig;
import ru.artegoser.enchatticus.integration.LuckPermsBridge;
import ru.artegoser.enchatticus.permission.PermissionService;
import ru.artegoser.enchatticus.tab.TabService;

import java.util.Map;

public final class ChatService {
    private final ConfigManager configManager;

    public ChatService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(this::onChatMessage);
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register(this::onCommandMessage);

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
            dispatcher.register(Commands.literal("enchatticus")
                .requires(source -> PermissionService.canReload(source, configManager.get()))
                .then(Commands.literal("reload").executes(context -> {
                    boolean ok = configManager.load();
                    if (ok) {
                        TabService.refreshAll(context.getSource().getServer());
                        context.getSource().sendSuccess(
                            () -> Component.literal("Enchatticus config reloaded: " + configManager.path()),
                            false
                        );
                        return 1;
                    }

                    context.getSource().sendFailure(Component.literal("Failed to reload Enchatticus config. Check the server log."));
                    return 0;
                }))
            )
        );
    }

    private boolean onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound boundChatType) {
        if (!boundChatType.chatType().is(ChatType.CHAT)) {
            return true;
        }

        route(sender, message.signedContent(), false);
        return false;
    }

    private boolean onCommandMessage(PlayerChatMessage message, net.minecraft.commands.CommandSourceStack source, ChatType.Bound boundChatType) {
        if (!boundChatType.chatType().is(ChatType.EMOTE_COMMAND)) {
            return true;
        }

        ServerPlayer sender = source.getPlayer();
        if (sender == null) {
            return true;
        }

        route(sender, message.signedContent(), true);
        return false;
    }

    private void route(ServerPlayer sender, String rawMessage, boolean emote) {
        EnchatticusConfig config = configManager.get();
        String trigger = config.chat.globalTrigger;

        boolean global = rawMessage.startsWith(trigger);
        String content = global ? rawMessage.substring(trigger.length()).stripLeading() : rawMessage;
        if (content.isBlank()) {
            return;
        }

        if (emote && !PermissionService.canUseMe(sender, config)) {
            deny(sender, config.permissions.me);
            return;
        }
        if (global && !PermissionService.canUseGlobal(sender, config)) {
            deny(sender, config.permissions.globalChat);
            return;
        }
        if (!global && !PermissionService.canUseLocal(sender, config)) {
            deny(sender, config.permissions.localChat);
            return;
        }

        LuckPermsBridge.Meta meta = LuckPermsBridge.meta(sender, config);
        Component badges = BadgeService.render(sender, config, false);
        String template;
        if (emote) {
            template = global ? config.chat.globalMeFormat : config.chat.localMeFormat;
        } else {
            template = global ? config.chat.globalFormat : config.chat.localFormat;
        }

        String channelPrefix = global ? config.chat.globalPrefix : config.chat.localPrefix;

        Component rendered = LegacyComponentFormatter.format(template, Map.of(
            "channel", LegacyComponentFormatter.Value.formatted(channelPrefix),
            "badges", LegacyComponentFormatter.Value.component(badges),
            "prefix", LegacyComponentFormatter.Value.formatted(meta.prefix()),
            "suffix", LegacyComponentFormatter.Value.formatted(meta.suffix()),
            "name", LegacyComponentFormatter.Value.plain(sender.getPlainTextName()),
            "message", LegacyComponentFormatter.Value.plain(content)
        ));

        MinecraftServer server = sender.level().getServer();
        server.sendSystemMessage(rendered);

        double radiusSquared = config.chat.localRadius * config.chat.localRadius;
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (global) {
                if (!config.chat.globalAcrossDimensions && receiver.level() != sender.level()) {
                    continue;
                }
            } else {
                if (receiver.level() != sender.level()) {
                    continue;
                }
                if (receiver.distanceToSqr(sender) > radiusSquared) {
                    continue;
                }
            }

            receiver.sendSystemMessage(rendered);
        }

        Enchatticus.LOGGER.debug("{} chat from {}: {}", global ? "Global" : "Local", sender.getPlainTextName(), content);
    }

    private static void deny(ServerPlayer player, String permission) {
        player.sendSystemMessage(Component.literal("Недостаточно прав: " + permission));
    }
}
