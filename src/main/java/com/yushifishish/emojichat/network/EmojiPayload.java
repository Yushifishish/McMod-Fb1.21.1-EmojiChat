package com.yushifishish.emojichat.network;

import com.yushifishish.emojichat.EmojiChat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EmojiPayload(String name, byte[] data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EmojiPayload> TYPE = new CustomPacketPayload.Type<>(EmojiChat.id("sync"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, EmojiPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, EmojiPayload::name,
            ByteBufCodecs.BYTE_ARRAY, EmojiPayload::data,
            EmojiPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { 
        return TYPE; 
    }
}
