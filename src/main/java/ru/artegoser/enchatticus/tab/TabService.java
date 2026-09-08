package ru.artegoser.enchatticus.tab;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import ru.artegoser.enchatticus.Enchatticus;
import ru.artegoser.enchatticus.badge.BadgeService;
import ru.artegoser.enchatticus.chat.LegacyComponentFormatter;
import ru.artegoser.enchatticus.config.EnchatticusConfig;
import ru.artegoser.enchatticus.integration.LuckPermsBridge;

import java.util.Map;
import java.util.UUID;

public final class TabService {
    private TabService() {
    }

    public static Component displayName(ServerPlayer player) {
        if (Enchatticus.configManager() == null) {
            return null;
        }

        EnchatticusConfig config = Enchatticus.configManager().get();
        if (!config.tab.enabled) {
            return null;
        }

        LuckPermsBridge.Meta meta = LuckPermsBridge.meta(player, config);
        Component badges = BadgeService.render(player, config, true);
        return LegacyComponentFormatter.format(config.tab.format, Map.of(
            "badges", LegacyComponentFormatter.Value.component(badges),
            "prefix", LegacyComponentFormatter.Value.formatted(meta.prefix()),
            "suffix", LegacyComponentFormatter.Value.formatted(meta.suffix()),
            "name", LegacyComponentFormatter.Value.plain(player.getPlainTextName())
        ));
    }

    public static void refreshAll(MinecraftServer server) {
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            refresh(server, target);
        }
    }

    public static void refreshPlayer(MinecraftServer server, UUID uuid) {
        ServerPlayer target = server.getPlayerList().getPlayer(uuid);
        if (target != null) {
            refresh(server, target);
        }
    }

    private static void refresh(MinecraftServer server, ServerPlayer target) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
            target
        );
        server.getPlayerList().broadcastAll(packet);
    }
}
