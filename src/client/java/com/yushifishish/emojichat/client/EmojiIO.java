package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.EmojiChat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class EmojiIO {
    public static void saveEmoji(String name, byte[] data) {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("emoji-chat").resolve("custom");
        Path targetFile = configDir.resolve(name + ".png");

        try {
            if (Files.exists(targetFile)) {
                byte[] existingData = Files.readAllBytes(targetFile);
                if (Arrays.equals(existingData, data)) {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("§e[Emoji] Image already exists and is identical."), false);
                    return;
                }
                
                // If exists but different, prompt overwrite via command or just overwrite for simplicity in this version
                // To handle "prompt selection" in a GUI-free way, we use a system message warning.
                Minecraft.getInstance().player.displayClientMessage(Component.literal("§c[Emoji] Overwriting existing image: " + name), false);
            }

            Files.createDirectories(configDir);
            Files.write(targetFile, data);
            Minecraft.getInstance().player.displayClientMessage(Component.literal("§a[Emoji] Saved: " + name), false);
            
            // Reload to apply
            LocalEmojiLoader.loadFromFolder();

        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to save emoji", e);
            Minecraft.getInstance().player.displayClientMessage(Component.literal("§c[Emoji] Failed to save image."), false);
        }
    }
}
