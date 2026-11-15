package com.yushifishish.emojichat.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatHudAddMessageMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component modifyAddedMessage(Component message) {
        String text = message.getString();
        if (text.matches(".*\\[[^\\]]+\\].*")) {
            // Add empty full-width spaces to push the chat upwards locally
            String fakeLines = "\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000";
            return Component.literal(text + fakeLines);
        }
        return message;
    }
}
