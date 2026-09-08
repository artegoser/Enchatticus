package ru.artegoser.enchatticus.badge;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import ru.artegoser.enchatticus.chat.LegacyComponentFormatter;
import ru.artegoser.enchatticus.config.EnchatticusConfig;
import ru.artegoser.enchatticus.integration.LuckPermsBridge;

import java.util.Map;

public final class BadgeService {
    private BadgeService() {
    }

    public static Component render(ServerPlayer player, EnchatticusConfig config, boolean tab) {
        MutableComponent root = Component.empty();
        for (EnchatticusConfig.Badge badge : config.badges) {
            if (tab ? !badge.showInTab : !badge.showInChat) {
                continue;
            }
            if (!isActive(player, badge)) {
                continue;
            }
            root.append(renderBadge(player, badge));
        }
        return root;
    }

    private static boolean isActive(ServerPlayer player, EnchatticusConfig.Badge badge) {
        boolean hasGroupCondition = !badge.group.isBlank();
        boolean hasPermissionCondition = !badge.permission.isBlank();

        if (!hasGroupCondition && !hasPermissionCondition) {
            return true;
        }
        if (hasGroupCondition && !LuckPermsBridge.inheritsGroup(player, badge.group)) {
            return false;
        }
        return !hasPermissionCondition || LuckPermsBridge.hasPermission(player, badge.permission, false);
    }

    private static Component renderBadge(ServerPlayer player, EnchatticusConfig.Badge badge) {
        String name = player.getPlainTextName();
        String command = replacePlaceholders(badge.clickCommand, name, badge.id);
        Map<String, LegacyComponentFormatter.Value> values = Map.of(
            "name", LegacyComponentFormatter.Value.plain(name),
            "command", LegacyComponentFormatter.Value.plain(command),
            "id", LegacyComponentFormatter.Value.plain(badge.id)
        );

        Component badgeText = LegacyComponentFormatter.format(badge.text, values);
        MutableComponent wrapped = Component.empty().append(badgeText);

        if (!badge.hover.isBlank()) {
            Component hover = LegacyComponentFormatter.format(badge.hover, values);
            wrapped.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(hover)));
        }

        if (!command.isBlank()) {
            String action = badge.clickAction.toLowerCase();
            switch (action) {
                case "run" -> wrapped.withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand(command)));
                case "copy" -> wrapped.withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(command)));
                default -> wrapped.withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand(command)));
            }
        }

        return wrapped;
    }

    private static String replacePlaceholders(String value, String name, String id) {
        if (value == null) {
            return "";
        }
        return value.replace("{name}", name).replace("{id}", id == null ? "" : id);
    }
}
