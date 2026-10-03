package ru.artegoser.enchatticus.config;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

final class AtomicConfigWriter {
    private AtomicConfigWriter() {}
    static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "enchatticus-", ".tmp");
        try {
            Files.writeString(temporary, content);
            try (var channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) { channel.force(true); }
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
