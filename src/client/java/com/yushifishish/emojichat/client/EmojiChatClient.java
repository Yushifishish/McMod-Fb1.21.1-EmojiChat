package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.EmojiChat;
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

		ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> {
			String text = message.getString();
			if (text.matches(".*\\[[^\\]]+\\].*")) {
				// 添加打印日志，你在 latest.log 里搜 "EMOJI DEBUG" 就能看到有没有触发
				EmojiChat.LOGGER.info("EMOJI DEBUG: Intercepted message! " + text);
				
				// 用不可见的全角空格和换行来强行占位撑开
				String fakeLines = "\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000";
				return Component.literal(text + fakeLines);
			}
			return message;
		});
	}
}
