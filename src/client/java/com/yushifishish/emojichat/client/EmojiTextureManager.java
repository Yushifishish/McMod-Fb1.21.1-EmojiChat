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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EmojiTextureManager {
    private static final Map<String, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> ORIGINAL_DATA = new ConcurrentHashMap<>();

    public static void put(String name, byte[] data) {
        try {
            ORIGINAL_DATA.put(name, data);
            
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
            
            // 👇 核心修复：强制把原图缩放到 96x96！
            int maxSize = 96;
            float ratio = Math.min((float) maxSize / image.getWidth(), (float) maxSize / image.getHeight());
            int newWidth = Math.max(1, (int) (image.getWidth() * ratio));
            int newHeight = Math.max(1, (int) (image.getHeight() * ratio));
            
            NativeImage scaledImage = new NativeImage(newWidth, newHeight, false);
            image.resizeSubRectTo(0, 0, image.getWidth(), image.getHeight(), scaledImage);
            image.close(); // 释放原图内存

            DynamicTexture texture = new DynamicTexture(scaledImage);
            String safePath = UUID.nameUUIDFromBytes(name.getBytes()).toString().replace("-", "");
            ResourceLocation id = EmojiChat.id("emoji/" + safePath);
            
            Minecraft.getInstance().getTextureManager().register(id, texture);
            TEXTURES.put(name, id);
            
        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to load emoji: " + name, e);
        }
    }

    public static void clear() { TEXTURES.clear(); ORIGINAL_DATA.clear(); }
    public static List<String> getAllNames() { return new ArrayList<>(TEXTURES.keySet()); }
    public static ResourceLocation getTexture(String name) { return TEXTURES.get(name); }
    public static byte[] getOriginal(String name) { return ORIGINAL_DATA.get(name); }
    public static boolean has(String name) { return TEXTURES.containsKey(name); }
}
