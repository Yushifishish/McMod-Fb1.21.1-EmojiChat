package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiChatClient;
import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected EditBox input;

    @Unique private boolean showSuggestions = false;
    @Unique private List<String> currentMatches;
    @Unique private int selectedIndex = 0;
    @Unique private int openBracketIndex = -1;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        String text = this.input.getValue();
        if (EmojiChatClient.isCommandState || text.startsWith("/")) {
            this.showSuggestions = false;
            return;
        }

        int lastOpen = text.lastIndexOf('[');
        if (lastOpen != -1 && text.indexOf(']', lastOpen) == -1) {
            String query = text.substring(lastOpen + 1).toLowerCase();
            this.currentMatches = EmojiTextureManager.getAllNames().stream()
                    .filter(name -> name.toLowerCase().contains(query))
                    .collect(Collectors.toList());
            if (!this.currentMatches.isEmpty()) {
                this.showSuggestions = true;
                this.openBracketIndex = lastOpen;
                if (this.selectedIndex >= this.currentMatches.size()) this.selectedIndex = 0;
            } else {
                this.showSuggestions = false;
            }
        } else {
            this.showSuggestions = false;
        }

        if (this.showSuggestions && this.currentMatches != null && !this.currentMatches.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.selectedIndex = (this.selectedIndex + 1) % this.currentMatches.size();
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                this.selectedIndex = (this.selectedIndex - 1 + this.currentMatches.size()) % this.currentMatches.size();
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER) {
                String selectedName = this.currentMatches.get(this.selectedIndex);
                this.input.setValue(text.substring(0, openBracketIndex) + "[" + selectedName + "]");
                this.showSuggestions = false;
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.showSuggestions = false;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.showSuggestions && this.currentMatches != null && !this.currentMatches.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            int x = this.input.getX();
            int baseY = this.input.getY() - 28;
            int total = this.currentMatches.size();

            for (int i = 0; i < Math.min(total, 5); i++) {
                String name = this.currentMatches.get(i);
                int y = baseY - (i * 22);
                guiGraphics.fill(x, y - 2, x + 120, y + 20, i == this.selectedIndex ? 0x80FFFFFF : 0x80000000);
                guiGraphics.blit(EmojiTextureManager.getTexture(name), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
                guiGraphics.drawString(mc.font, "[" + name + "]", x + 24, y + 6, 0xFFFFFF, false);
            }
        }
    }
}
