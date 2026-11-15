package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
import com.yushifishish.emojichat.client.EmojiIO;
import com.yushifishish.emojichat.client.EmojiPreviewScreen;
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

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected EditBox input;

    @Unique private boolean showSuggestions = false;
    @Unique private List<String> currentMatches;
    @Unique private int selectedIndex = 0;
    @Unique private int openBracketIndex = -1;
    @Unique private float smoothScroll = 0.0f;
    @Unique private long lastKeyPressTime = 0;
    @Unique private long lastClickTime = 0;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        String text = this.input.getValue();
        
        // Release the slash command completely to vanilla
        if (text.startsWith("/")) {
            this.showSuggestions = false;
            return; 
        }

        // Update emoji matches dynamically
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
            long now = System.currentTimeMillis();
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.selectedIndex = (this.selectedIndex + 1) % this.currentMatches.size();
                if (this.selectedIndex == 0) this.smoothScroll -= this.currentMatches.size();
                this.lastKeyPressTime = now;
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                this.selectedIndex = (this.selectedIndex - 1 + this.currentMatches.size()) % this.currentMatches.size();
                if (this.selectedIndex == this.currentMatches.size() - 1) this.smoothScroll += this.currentMatches.size();
                this.lastKeyPressTime = now;
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

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        for (EmojiClickArea area : EmojiClickData.CLICK_AREAS) {
            if (mouseX >= area.x && mouseX <= area.x + area.width && mouseY >= area.y && mouseY <= area.y + area.height) {
                if (button == 0) { 
                    long now = System.currentTimeMillis();
                    if (now - this.lastClickTime < 300) { 
                        Minecraft.getInstance().setScreen(new EmojiPreviewScreen(area.name));
                    }
                    this.lastClickTime = now;
                } else if (button == 1) { 
                    EmojiIO.saveEmoji(area.name, EmojiTextureManager.getOriginal(area.name));
                }
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (this.showSuggestions && this.currentMatches != null && !this.currentMatches.isEmpty()) {
            if (verticalAmount > 0) {
                this.selectedIndex = (this.selectedIndex - 1 + this.currentMatches.size()) % this.currentMatches.size();
                if (this.selectedIndex == this.currentMatches.size() - 1) this.smoothScroll += this.currentMatches.size();
            } else if (verticalAmount < 0) {
                this.selectedIndex = (this.selectedIndex + 1) % this.currentMatches.size();
                if (this.selectedIndex == 0) this.smoothScroll -= this.currentMatches.size();
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.showSuggestions && this.currentMatches != null && !this.currentMatches.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            int cursorPos = this.input.getCursorPosition();
            int textWidthBeforeCursor = mc.font.width(this.input.getValue().substring(0, Math.min(cursorPos, this.input.getValue().length())));
            
            int menuWidth = 120;
            int x = Math.max(2, Math.min(mc.getWindow().getGuiScaledWidth() - menuWidth - 2, this.input.getX() + textWidthBeforeCursor));
            int itemHeight = 22;
            int centerY = this.input.getY() - 6 - 20 - (2 * itemHeight);
            int total = this.currentMatches.size();
            
            float targetScroll = (float) this.selectedIndex;
            float lerpFactor = (System.currentTimeMillis() - this.lastKeyPressTime < 150) ? 0.45f : 0.08f;
            float diff = targetScroll - this.smoothScroll;
            if (diff > total / 2.0f) diff -= total;
            if (diff < -total / 2.0f) diff += total;

            float moveAmount = diff * lerpFactor;
            if (moveAmount > 1.0f) moveAmount = 1.0f;
            else if (moveAmount < -1.0f) moveAmount = -1.0f;
            
            this.smoothScroll += moveAmount;
            if (Math.abs(targetScroll - this.smoothScroll) < 0.02f) this.smoothScroll = targetScroll;

            int boxTop = centerY - (2 * itemHeight) - 4;
            int boxBottom = centerY + (2 * itemHeight) + itemHeight + 2;
            guiGraphics.enableScissor(x, boxTop, x + menuWidth, boxBottom);

            for (int i = -3; i <= 3; i++) {
                int actualIndex = (int)Math.round(this.smoothScroll) + i;
                float offset = actualIndex - this.smoothScroll;
                if (offset < -3 || offset > 3) continue;
                
                int wrappedIndex = (actualIndex % total + total) % total;
                String name = this.currentMatches.get(wrappedIndex);
                int y = centerY + (int)(offset * itemHeight);

                if (wrappedIndex == this.selectedIndex) guiGraphics.fill(x, y - 2, x + menuWidth, y + 20, 0x80FFFFFF);
                else guiGraphics.fill(x, y - 2, x + menuWidth, y + 20, 0x80000000);

                if (EmojiTextureManager.has(name)) guiGraphics.blit(EmojiTextureManager.getTexture(name), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
                guiGraphics.drawString(mc.font, "[" + name + "]", x + 24, y + 6, 0xFFFFFF, false);
            }
            guiGraphics.disableScissor();
        }
    }
}
