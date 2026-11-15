package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.EmojiChat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;

public class EmojiIO {
    public static void saveEmoji(String name, byte[] data) {
        if (data == null) return;
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("emoji-chat").resolve("custom");
        Path targetFile = configDir.resolve(name + ".png");

        try {
            if (Files.exists(targetFile)) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§c[Emoji] Image already exists, skipping save."), false);
                return;
            }

            Files.createDirectories(configDir);
            Files.write(targetFile, data);
            Minecraft.getInstance().player.displayClientMessage(Component.literal("§a[Emoji] Saved: " + name), false);

        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to save emoji", e);
        }
    }
}
