package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.network.EmojiPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EmojiChatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		
		// Register the client receiver to handle incoming emoji payloads
		ClientPlayNetworking.registerGlobalReceiver(EmojiPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EmojiTextureManager.put(payload.name(), payload.data()));
		});
	}
}
