package com.yushifishish.emojichat.client;

import com.yushifishish.emojichat.network.EmojiPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EmojiChatClient implements ClientModInitializer {

    // 全局标志：当前是否处于“指令输入状态”
    public static boolean isCommandState = false;

    @Override
    public void onInitializeClient() {
        // 网络接收
        ClientPlayNetworking.registerGlobalReceiver(EmojiPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> EmojiTextureManager.put(payload.name(), payload.data()));
        });

        // 延迟加载
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            LocalEmojiLoader.loadFromFolder();
        });

        // ✅ 关键：监听“即将发送指令”事件，并设置标志位
        ClientSendMessageEvents.ALLOW_COMMAND.register((command) -> {
            isCommandState = true;
            return true; // 不拦截，允许发送
        });

        // 当发送聊天消息时，重置状态
        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            isCommandState = false;
            return true;
        });
    }
}
