package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.EmojiChat;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class LocalEmojiLoader {
    public static void loadFromFolder() {
        // 获取 config/emoji-chat/custom 目录
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("emoji-chat").resolve("custom");
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir); // 不存在则创建
            } catch (Exception e) {
                EmojiChat.LOGGER.error("Failed to create emoji folder", e);
                return;
            }
        }

        // 遍历文件夹下的所有 .png 文件
        try (Stream<Path> paths = Files.list(configDir)) {
            paths.filter(path -> path.toString().toLowerCase().endsWith(".png"))
                 .forEach(path -> {
                     try {
                         String fileName = path.getFileName().toString();
                         String emojiName = fileName.substring(0, fileName.length() - 4); // 去掉 .png
                         byte[] imageBytes = Files.readAllBytes(path);
                         // 调用已有的 put 方法注册纹理
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
