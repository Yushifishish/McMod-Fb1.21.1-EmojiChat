package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.network.EmojiPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EmojiChatClient implements ClientModInitializer {
	
	private boolean testLoaded = false;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(EmojiPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EmojiTextureManager.put(payload.name(), payload.data()));
		});

		// 延迟到玩家进入世界后再加载纹理，确保 OpenGL 环境就绪
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!testLoaded && client.player != null) {
				EmojiTextureManager.putTest();
				testLoaded = true;
			}
		});
	}
}
