package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiTextureManager;
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
            if (text.endsWith("[")) {
                String name = "test"; // Hardcoded for testing
                if (EmojiTextureManager.has(name)) {
                    this.input.setValue(text.substring(0, text.length() - 1) + "[" + name + "]");
                }
            }
        });
    }
}
