package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.EmojiChat;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LocalEmojiLoader {
    public static void loadFromFolder() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("emoji-chat").resolve("custom");
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (Exception e) {
                EmojiChat.LOGGER.error("Failed to create emoji folder", e);
                return;
            }
        }

        try (Stream<Path> paths = Files.list(configDir)) {
            paths.filter(path -> path.toString().toLowerCase().endsWith(".png"))
                 .forEach(path -> {
                     try {
                         String fileName = path.getFileName().toString();
                         String emojiName = fileName.substring(0, fileName.length() - 4);
                         
                         // Rule: Ban formats like [i], [ii], [iii] as requested originally.
                         // Note: Chinese and other Unicode characters are now allowed in filenames.
                         if (emojiName.matches("^[iI]+$")) {
                             EmojiChat.LOGGER.warn("Skipped forbidden emoji name: " + fileName);
                             return;
                         }

                         byte[] imageBytes = Files.readAllBytes(path);
                         EmojiTextureManager.put(emojiName, imageBytes);
                         
                     } catch (Exception e) {
                         EmojiChat.LOGGER.error("Failed to load emoji: " + path, e);
                     }
                 });
        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to scan emoji folder", e);
        }
    }
}
