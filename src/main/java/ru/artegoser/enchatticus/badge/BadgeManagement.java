package ru.artegoser.enchatticus.badge;

import java.io.IOException;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerPlayer;
import ru.artegoser.enchatticus.Enchatticus;
import ru.artegoser.enchatticus.config.ConfigManager;
import ru.artegoser.enchatticus.config.EnchatticusConfig;
import ru.artegoser.enchatticus.integration.LuckPermsBridge;

/** Optional Fabric entrypoint for consumers. Permission checks for editing belong to the caller. */
public final class BadgeManagement {
    private static volatile BiPredicate<ServerPlayer, String> chatVisibility = (player, id) -> true;

    public static boolean visibleInChat(ServerPlayer player, String id) { return chatVisibility.test(player, id); }
    public void chatVisibility(BiPredicate<ServerPlayer, String> filter) {
        chatVisibility = filter == null ? (player, id) -> true : filter;
    }
    public boolean eligible(ServerPlayer player, String id) {
        return Enchatticus.configManager().get().badges.stream().anyMatch(badge ->
                badge.id.equals(id) && customizable(player, badge));
    }
    private static boolean customizable(ServerPlayer player, EnchatticusConfig.Badge badge) {
        return BadgeCustomizationPolicy.permits(badge, () -> BadgeService.isActive(player, badge),
                permission -> LuckPermsBridge.hasPermission(player, permission, false));
    }
    public List<BadgeDefinition> list(ServerPlayer player, boolean administrative) {
        return Enchatticus.configManager().get().badges.stream()
                .filter(badge -> administrative || customizable(player, badge))
                .map(BadgeManagement::definition).toList();
    }
    public void upsert(BadgeDefinition definition) throws IOException {
        upsert(Enchatticus.configManager(), definition);
    }
    static void upsert(ConfigManager manager, BadgeDefinition definition) throws IOException {
        definition.validate();
        manager.editBadges(badges -> {
            EnchatticusConfig.Badge badge = badges.stream().filter(value -> value.id.equals(definition.id())).findFirst().orElse(null);
            if (badge == null) {
                if (badges.size() >= 256) throw new IllegalArgumentException("At most 256 badges are supported");
                badge = new EnchatticusConfig.Badge();
                badge.showInTab = false;
                badges.add(badge);
            }
            badge.id = definition.id(); badge.text = definition.text(); badge.hover = definition.hover();
            badge.group = definition.group(); badge.permission = definition.permission();
            badge.clickCommand = definition.clickCommand(); badge.clickAction = definition.clickAction();
            badge.showInChat = definition.showInChat();
            badge.customizable = definition.customizable(); badge.customizePermission = definition.customizePermission();
        });
    }
    public void disableChat(String id) throws IOException {
        Enchatticus.configManager().editBadges(badges -> badges.stream().filter(value -> value.id.equals(id))
                .forEach(badge -> badge.showInChat = false));
    }
    private static BadgeDefinition definition(EnchatticusConfig.Badge badge) {
        return new BadgeDefinition(badge.id, badge.text, badge.hover, badge.group, badge.permission,
                badge.clickCommand, badge.clickAction, badge.showInChat, badge.customizable, badge.customizePermission);
    }
}
