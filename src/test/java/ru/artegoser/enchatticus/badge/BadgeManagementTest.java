package ru.artegoser.enchatticus.badge;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.artegoser.enchatticus.config.ConfigManager;
import static org.junit.jupiter.api.Assertions.*;

class BadgeManagementTest {
    @TempDir Path directory;
    @Test void editsPreserveChatConfigurationAndExistingTabVisibility() throws Exception {
        ConfigManager manager = new ConfigManager(directory.resolve("enchatticus.json"));
        manager.get().chat.localFormat = "custom-chat";
        manager.get().tab.format = "custom-tab";
        BadgeManagement.upsert(manager, badge("police", "updated"));
        assertEquals("custom-chat", manager.get().chat.localFormat);
        assertEquals("custom-tab", manager.get().tab.format);
        assertTrue(manager.get().badges.getFirst().showInTab);
        BadgeManagement.upsert(manager, badge("new", "new badge"));
        assertFalse(manager.get().badges.getLast().showInTab);
        assertTrue(Files.readString(manager.path()).contains("custom-chat"));
        assertTrue(Files.readString(manager.path()).contains("new badge"));
    }
    @Test void failedPersistenceDoesNotPublishEdits() throws Exception {
        Path blocker = directory.resolve("file");
        Files.writeString(blocker, "blocking parent");
        ConfigManager manager = new ConfigManager(blocker.resolve("config.json"));
        String previous = manager.get().badges.getFirst().text;
        assertThrows(java.io.IOException.class, () -> BadgeManagement.upsert(manager, badge("police", "changed")));
        assertEquals(previous, manager.get().badges.getFirst().text);
        assertEquals("blocking parent", Files.readString(blocker));
    }
    @Test void invalidEditsCannotChangeStoredConfiguration() throws Exception {
        ConfigManager manager = new ConfigManager(directory.resolve("config.json"));
        manager.save();
        String previous = Files.readString(manager.path());
        assertThrows(IllegalArgumentException.class, () -> BadgeManagement.upsert(manager, badge("../bad", "bad")));
        assertEquals(previous, Files.readString(manager.path()));
        assertThrows(IllegalArgumentException.class, () -> new BadgeDefinition("good", "x".repeat(513), "", "", "", "", "run", true).validate());
    }
    private static BadgeDefinition badge(String id, String text) {
        return new BadgeDefinition(id, text, "", "", "", "", "suggest", true);
    }
}
