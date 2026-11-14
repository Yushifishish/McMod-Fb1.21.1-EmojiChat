package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected EditBox input;
    
    @Unique private boolean showSuggestions = false;
    @Unique private final String testEmojiName = "test";

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.input.setResponder(text -> {
            if (text.startsWith("[") && !text.contains("]")) {
                this.showSuggestions = true;
                if (!EmojiTextureManager.has(testEmojiName)) {
                    EmojiTextureManager.putTest();
                }
            } else {
                this.showSuggestions = false;
            }
        });
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.showSuggestions) {
            if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER) {
                this.input.setValue("[" + testEmojiName + "]");
                this.showSuggestions = false;
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.showSuggestions = false;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.showSuggestions) {
            int x = 4;
            int y = this.input.getY() - 28;
            
            // Draw background
            guiGraphics.fill(x, y, x + 110, y + 20, 0x80000000);
            
            // Draw red square image preview
            if (EmojiTextureManager.has(testEmojiName)) {
                guiGraphics.blit(EmojiTextureManager.get(testEmojiName), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
            }
            
            // Draw English text
            Minecraft mc = Minecraft.getInstance();
            guiGraphics.drawString(mc.font, "[" + testEmojiName + "]", x + 24, y + 6, 0xFFFFFF, false);
            guiGraphics.drawString(mc.font, "§7Press Tab to complete", x + 2, y - 10, 0xAAAAAA, false);
        }
    }
}
