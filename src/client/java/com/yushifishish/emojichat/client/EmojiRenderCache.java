package com.yushifishish.emojichat.client;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EmojiRenderCache {

    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    static {
        TEXTURE_CACHE.put("smile", ResourceLocation.fromNamespaceAndPath("emojichat", "textures/emoji/smile.png"));
        TEXTURE_CACHE.put("heart", ResourceLocation.fromNamespaceAndPath("emojichat", "textures/emoji/heart.png"));
        TEXTURE_CACHE.put("cry", ResourceLocation.fromNamespaceAndPath("emojichat", "textures/emoji/cry.png"));
    }

    public static ResourceLocation getTexture(String name) {
        return TEXTURE_CACHE.get(name);
    }

    public static Set<String> getAllEmojiNames() {
        return TEXTURE_CACHE.keySet();
    }

    public static void register(String name, ResourceLocation texture) {
        TEXTURE_CACHE.put(name, texture);
    }
}
