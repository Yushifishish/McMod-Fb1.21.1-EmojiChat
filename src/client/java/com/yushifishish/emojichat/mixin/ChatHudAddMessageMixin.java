package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public class ChatHudAddMessageMixin {

    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At("HEAD"), argsOnly = true, ordinal = 0
    )
    private Component modifyAddedMessage(Component message) {
        String text = message.getString();
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(text);

        // 只负责检测表情并撑开聊天框
        while (matcher.find()) {
            String emojiName = matcher.group(1);
            if (EmojiTextureManager.has(emojiName)) {
                String fakeLines = "\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000\n\u3000";
                return Component.literal(text + fakeLines);
            }
        }
        return message;
    }
}
