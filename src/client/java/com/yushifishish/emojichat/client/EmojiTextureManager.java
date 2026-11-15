package com.yushifishish.emojichat.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.yushifishish.emojichat.EmojiChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmojiTextureManager {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();
    private static final int MAX_SIZE = 48; // 最大限制 48x48

    public static void put(String name, byte[] data) {
        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
            
            int width = image.getWidth();
            int height = image.getHeight();
            if (width > MAX_SIZE || height > MAX_SIZE) {
                float ratio = Math.min((float) MAX_SIZE / width, (float) MAX_SIZE / height);
                int newWidth = Math.max(1, (int) (width * ratio));
                int newHeight = Math.max(1, (int) (height * ratio));
                
                NativeImage scaledImage = new NativeImage(newWidth, newHeight, false);
                image.resizeSubRectTo(0, 0, width, height, scaledImage);
                image.close();
                image = scaledImage;
            }

            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = EmojiChat.id("emoji/" + name);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            CACHE.put(name, id);
        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to load emoji: " + name, e);
        }
    }

    public static List<String> getAllNames() {
        return new ArrayList<>(CACHE.keySet());
    }

    public static ResourceLocation get(String name) { return CACHE.get(name); }
    public static boolean has(String name) { return CACHE.containsKey(name); }
}
