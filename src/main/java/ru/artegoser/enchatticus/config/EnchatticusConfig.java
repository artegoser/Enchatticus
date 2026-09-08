package ru.artegoser.enchatticus.config;

import java.util.ArrayList;
import java.util.List;

public final class EnchatticusConfig {
    public Chat chat = new Chat();
    public Tab tab = new Tab();
    public LuckPerms luckPerms = new LuckPerms();
    public Permissions permissions = new Permissions();
    public List<Badge> badges = defaultBadges();

    public static final class Chat {
        /** Radius of the local chat in blocks. */
        public double localRadius = 64.0;

        /** A message beginning with this string is sent to the global channel. */
        public String globalTrigger = "!";

        /** Global chat reaches every online player on the server. */
        public boolean globalAcrossDimensions = true;

        /** Visible channel prefixes. */
        public String localPrefix = "&8[Л] &r";
        public String globalPrefix = "&6[Г] &r";

        public String localFormat = "{channel}{badges}{prefix}{name}{suffix}&7: &f{message}";
        public String globalFormat = "{channel}{badges}{prefix}{name}{suffix}&7: &f{message}";
        public String localMeFormat = "{channel}* {badges}{prefix}{name}{suffix} &7{message}";
        public String globalMeFormat = "{channel}* {badges}{prefix}{name}{suffix} &f{message}";
    }

    public static final class Tab {
        /** Show Enchatticus formatting in the player list. */
        public boolean enabled = true;

        /** Available placeholders: {badges}, {prefix}, {name}, {suffix}. */
        public String format = "{badges}{prefix}{name}{suffix}";
    }

    public static final class LuckPerms {
        /** If LuckPerms is installed, use its resolved meta prefix in {prefix}. */
        public boolean usePrefix = true;

        /** Optional resolved LuckPerms meta suffix in {suffix}. */
        public boolean useSuffix = false;
    }

    public static final class Permissions {
        /** Permission nodes. Undefined LuckPerms nodes fall back to these defaults. */
        public String localChat = "enchatticus.chat.local";
        public String globalChat = "enchatticus.chat.global";
        public String me = "enchatticus.chat.me";
        public String reload = "enchatticus.command.reload";

        public boolean localChatDefault = true;
        public boolean globalChatDefault = true;
        public boolean meDefault = true;
    }

    public static final class Badge {
        /** Stable config id, only used for diagnostics/documentation. */
        public String id = "badge";

        /** Formatted badge text. Add trailing space here if desired. */
        public String text = "&7[Badge] &r";

        /** Optional hover text. Supports colors and {name}/{command}/{id}. */
        public String hover = "";

        /** Optional LuckPerms group. Inherited membership counts. */
        public String group = "";

        /** Optional LuckPerms permission node. If group + permission are both set, both are required. */
        public String permission = "";

        /** Optional command for clicking the badge. {name} is replaced with the player name. */
        public String clickCommand = "";

        /** suggest | run | copy */
        public String clickAction = "suggest";

        public boolean showInChat = true;
        public boolean showInTab = true;
    }

    private static List<Badge> defaultBadges() {
        List<Badge> result = new ArrayList<>();
        Badge police = new Badge();
        police.id = "police";
        police.text = "&9👮 &r";
        police.hover = "&9Полицейский\n&7Сотрудник полиции";
        police.group = "police";
        result.add(police);
        return result;
    }
}
