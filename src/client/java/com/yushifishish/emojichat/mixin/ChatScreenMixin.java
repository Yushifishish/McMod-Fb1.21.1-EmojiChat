package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiSuggestScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.input.setResponder(text -> {
            // 如果最后输入的字符是 '['
            if (text.endsWith("[")) {
                // 打开我们的候选菜单，并把 '[' 前面的文字传过去
                String beforeBracket = text.substring(0, text.length() - 1);
                net.minecraft.client.Minecraft.getInstance().setScreen(
                    new EmojiSuggestScreen((ChatScreen) (Object) this, beforeBracket)
                );
            }
        });
    }
}
