package com.yushifishish.emojichat;

import com.yushifishish.emojichat.network.EmojiPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmojiChat implements ModInitializer {
	public static final String MOD_ID = "emoji-chat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Emoji Chat is initializing network payloads...");
		PayloadTypeRegistry.playS2C().register(EmojiPayload.TYPE, EmojiPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(EmojiPayload.TYPE, EmojiPayload.CODEC);
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
