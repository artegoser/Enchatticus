package ru.artegoser.enchatticus.config;

public final class EnchatticusConfig {
    public Chat chat = new Chat();
    public LuckPerms luckPerms = new LuckPerms();

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

        public String localFormat = "{channel}{prefix}{name}{suffix}&7: &f{message}";
        public String globalFormat = "{channel}{prefix}{name}{suffix}&7: &f{message}";
        public String localMeFormat = "{channel}* {prefix}{name}{suffix} &7{message}";
        public String globalMeFormat = "{channel}* {prefix}{name}{suffix} &f{message}";
    }

    public static final class LuckPerms {
        /** If LuckPerms is installed, use its resolved meta prefix. */
        public boolean usePrefix = true;

        /** Optional, disabled by default. */
        public boolean useSuffix = false;
    }
}
