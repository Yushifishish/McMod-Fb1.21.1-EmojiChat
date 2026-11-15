package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.network.EmojiPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;

public class EmojiChatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(EmojiPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EmojiTextureManager.put(payload.name(), payload.data()));
		});

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			LocalEmojiLoader.loadFromFolder();
		});

		// Local Display Trick: Add empty lines ONLY on the local client to expand the chat height
		ClientReceiveMessageEvents.MODIFY_GAME.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			String text = message.getString();
			// If the message contains an emoji pattern
			if (text.matches(".*\\[[^\\]]+\\].*")) {
				// Append 10 empty lines to expand the chat box height locally
				return Component.literal(text + "\n\n\n\n\n\n\n\n\n\n");
			}
			return message;
		});
	}
}
