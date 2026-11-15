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
		ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
            String text = message.getString();
            if (text.matches(".*\\[[^\\]]+\\].*")) {
                return Component.literal(text + "\n\n\n\n\n\n\n\n\n\n");
            }
            return message;
        });
	}
}
