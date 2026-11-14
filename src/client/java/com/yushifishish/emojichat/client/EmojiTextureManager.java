package com.yushifishish.emojichat.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.yushifishish.emojichat.EmojiChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmojiTextureManager {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    public static void put(String name, byte[] data) {
        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = EmojiChat.id("emoji/" + name);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            CACHE.put(name, id);
        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to load emoji: " + name, e);
        }
    }

    // 临时测试方法：用代码画一张红色图片
    public static void putTest() {
        try {
            NativeImage image = new NativeImage(16, 16, false);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    image.setPixelRGBA(x, y, 0xFFFF0000); // 红色
                }
            }
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation id = EmojiChat.id("emoji/test");
            Minecraft.getInstance().getTextureManager().register(id, texture);
            CACHE.put("test", id);
        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to load test emoji", e);
        }
    }

    public static ResourceLocation get(String name) { return CACHE.get(name); }
    public static boolean has(String name) { return CACHE.containsKey(name); }
}
