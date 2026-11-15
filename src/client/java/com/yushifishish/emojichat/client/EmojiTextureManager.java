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
    private static final Map<String, ResourceLocation> SMALL_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ResourceLocation> LARGE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> ORIGINAL_DATA = new ConcurrentHashMap<>();
    
    private static final int SMALL_SIZE = 48;
    private static final int LARGE_SIZE = 128;

    public static void put(String name, byte[] data) {
        try {
            ORIGINAL_DATA.put(name, data);

            NativeImage smallImage = NativeImage.read(new ByteArrayInputStream(data));
            DynamicTexture smallTex = new DynamicTexture(resizeImage(smallImage, SMALL_SIZE));
            ResourceLocation smallId = EmojiChat.id("emoji/small_" + getSafePath(name));
            Minecraft.getInstance().getTextureManager().register(smallId, smallTex);
            SMALL_CACHE.put(name, smallId);
            smallImage.close();

            NativeImage largeImage = NativeImage.read(new ByteArrayInputStream(data));
            DynamicTexture largeTex = new DynamicTexture(resizeImage(largeImage, LARGE_SIZE));
            ResourceLocation largeId = EmojiChat.id("emoji/large_" + getSafePath(name));
            Minecraft.getInstance().getTextureManager().register(largeId, largeTex);
            LARGE_CACHE.put(name, largeId);
            largeImage.close();
            
        } catch (Exception e) {
            EmojiChat.LOGGER.error("Failed to load emoji: " + name, e);
        }
    }

    private static NativeImage resizeImage(NativeImage image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= maxSize && height <= maxSize) {
            return image;
        }
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.max(1, (int) (width * ratio));
        int newHeight = Math.max(1, (int) (height * ratio));
        NativeImage scaled = new NativeImage(newWidth, newHeight, false);
        image.resizeSubRectTo(0, 0, width, height, scaled);
        return scaled;
    }

    private static String getSafePath(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString().replace("-", "");
    }

    public static void clear() {
        SMALL_CACHE.clear();
        LARGE_CACHE.clear();
        ORIGINAL_DATA.clear();
    }

    public static List<String> getAllNames() { return new ArrayList<>(SMALL_CACHE.keySet()); }
    public static ResourceLocation getSmall(String name) { return SMALL_CACHE.get(name); }
    public static ResourceLocation getLarge(String name) { return LARGE_CACHE.get(name); }
    public static byte[] getOriginal(String name) { return ORIGINAL_DATA.get(name); }
    public static boolean has(String name) { return SMALL_CACHE.containsKey(name); }
}
