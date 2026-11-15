package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.network.EmojiPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiChatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(EmojiPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EmojiTextureManager.put(payload.name(), payload.data()));
		});

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			LocalEmojiLoader.loadFromFolder();
		});

		// Intercept incoming chat messages to add interactivity
		ClientReceiveMessageEvents.MODIFY_GAME.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			String text = message.getString();
			Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
			Matcher matcher = pattern.matcher(text);
			
			if (matcher.find()) {
				Component newMessage = Component.empty();
				int lastEnd = 0;
				matcher.reset();
				
				while (matcher.find()) {
					String emojiName = matcher.group(1);
					// Add text before the emoji
					newMessage = newMessage.copy().append(Component.literal(text.substring(lastEnd, matcher.start())));
					
					if (EmojiTextureManager.has(emojiName)) {
						// Create a clickable component for the emoji
						Style style = Style.EMPTY
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emojichat preview " + emojiName))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to preview: " + emojiName)));
						newMessage = newMessage.copy().append(Component.literal("[" + emojiName + "]").withStyle(style));
					} else {
						newMessage = newMessage.copy().append(Component.literal("[" + emojiName + "]"));
					}
					lastEnd = matcher.end();
				}
				newMessage = newMessage.copy().append(Component.literal(text.substring(lastEnd)));
				return newMessage;
			}
			return message;
		});
	}
}
