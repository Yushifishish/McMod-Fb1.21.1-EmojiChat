package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.EmojiChat;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatHudAddMessageMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component modifyAddedMessage(Component message) {
        String text = message.getString();
        if (text.matches(".*\\[[^\\]]+\\].*")) {
            EmojiChat.LOGGER.info("EMOJI DEBUG: addMessage intercepted! " + text);
            String fakeLines = "\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000";
            return Component.literal(text + fakeLines);
        }
        return message;
    }
}
