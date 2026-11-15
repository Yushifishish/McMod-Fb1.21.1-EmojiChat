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

    @Unique
    private void updateSuggestions(String text) {
        // If the user is typing a command, completely disable emoji suggestions to let vanilla handle it
        if (text.startsWith("/")) {
            this.showSuggestions = false;
            this.openBracketIndex = -1;
            this.selectedIndex = 0;
            this.smoothScroll = 0;
            return;
        }

        int lastOpen = text.lastIndexOf('[');

        if (lastOpen != -1) {
            int closeHalf = text.indexOf(']', lastOpen);
            boolean hasClose = (closeHalf != -1);

            if (!hasClose) {
                String query = text.substring(lastOpen + 1).toLowerCase();
                
                currentMatches = EmojiTextureManager.getAllNames().stream()
                        .filter(name -> name.toLowerCase().contains(query))
                        .collect(Collectors.toList());

                if (!currentMatches.isEmpty()) {
                    this.showSuggestions = true;
                    this.openBracketIndex = lastOpen;
                    if (this.selectedIndex >= currentMatches.size()) {
                        this.selectedIndex = 0;
                        this.smoothScroll = 0; 
                    }
                    return;
                }
            }
        }
        
        this.showSuggestions = false;
        this.openBracketIndex = -1;
        this.selectedIndex = 0;
        this.smoothScroll = 0;
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        // Update state based on current text
        this.updateSuggestions(this.input.getValue());

        // If it's a command, do not intercept anything
        if (this.input.getValue().startsWith("/")) {
            return;
        }

        if (this.showSuggestions && currentMatches != null && !currentMatches.isEmpty()) {
            long now = System.currentTimeMillis();
            
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.selectedIndex = (this.selectedIndex + 1) % currentMatches.size();
                if (this.selectedIndex == 0) {
                    this.smoothScroll -= currentMatches.size();
                }
                this.lastKeyPressTime = now;
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                this.selectedIndex = (this.selectedIndex - 1 + currentMatches.size()) % currentMatches.size();
                if (this.selectedIndex == currentMatches.size() - 1) {
                    this.smoothScroll += currentMatches.size();
                }
                this.lastKeyPressTime = now;
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER) {
                String selectedName = currentMatches.get(this.selectedIndex);
                String text = input.getValue();
                
                String beforeBracket = text.substring(0, openBracketIndex);
                this.input.setValue(beforeBracket + "[" + selectedName + "]");
                
                this.showSuggestions = false;
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.showSuggestions = false;
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (this.showSuggestions && currentMatches != null && !currentMatches.isEmpty()) {
            long now = System.currentTimeMillis();
            if (verticalAmount > 0) {
                this.selectedIndex = (this.selectedIndex - 1 + currentMatches.size()) % currentMatches.size();
                if (this.selectedIndex == currentMatches.size() - 1) this.smoothScroll += currentMatches.size();
            } else if (verticalAmount < 0) {
                this.selectedIndex = (this.selectedIndex + 1) % currentMatches.size();
                if (this.selectedIndex == 0) this.smoothScroll -= currentMatches.size();
            }
            this.lastKeyPressTime = now;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Update state continuously without overriding vanilla responder
        this.updateSuggestions(this.input.getValue());

        if (this.showSuggestions && currentMatches != null && !currentMatches.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            
            String text = this.input.getValue();
            int cursorPos = this.input.getCursorPosition();
            int textWidthBeforeCursor = mc.font.width(text.substring(0, Math.min(cursorPos, text.length())));
            
            int menuWidth = 120;
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int cursorX = this.input.getX() + textWidthBeforeCursor;
            int x = Math.max(2, Math.min(screenWidth - menuWidth - 2, cursorX));
            
            int total = currentMatches.size();
            int maxVisible = 5; 
            int itemHeight = 22;

            int centerY = this.input.getY() - 6 - 20 - (2 * itemHeight);
            
            float targetScroll = (float) this.selectedIndex;
            float lerpFactor = (System.currentTimeMillis() - this.lastKeyPressTime < 150) ? 0.6f : 0.25f;
            
            float diff = targetScroll - this.smoothScroll;
            if (diff > total / 2.0f) diff -= total;
            if (diff < -total / 2.0f) diff += total;

            this.smoothScroll += diff * lerpFactor;
            
            if (Math.abs(targetScroll - this.smoothScroll) < 0.01f) {
                this.smoothScroll = targetScroll;
            }

            int boxTop = centerY - (2 * itemHeight) - 4;
            int boxBottom = centerY + (2 * itemHeight) + itemHeight + 2;
            guiGraphics.enableScissor(x, boxTop, x + menuWidth, boxBottom);

            for (int i = -3; i <= 7; i++) {
                int actualIndex = (int) Math.round(this.smoothScroll) + i;
                actualIndex = (actualIndex % total + total) % total;

                String name = currentMatches.get(actualIndex);
                
                float floatOffset = i - (this.smoothScroll - Math.round(this.smoothScroll));
                int y = centerY - (int)(floatOffset * itemHeight);

                if (i >= -2 && i <= 2) {
                    if (actualIndex == this.selectedIndex) {
                        guiGraphics.fill(x, y - 2, x + menuWidth, y + 20, 0x80FFFFFF);
                    } else {
                        guiGraphics.fill(x, y - 2, x + menuWidth, y + 20, 0x80000000);
                    }
                }

                if (EmojiTextureManager.has(name)) {
                    guiGraphics.blit(EmojiTextureManager.get(name), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
                }
                
                guiGraphics.drawString(mc.font, "[" + name + "]", x + 24, y + 6, 0xFFFFFF, false);
            }

            guiGraphics.disableScissor();

            String hint = "§7Use UP/DOWN or SCROLL, TAB to complete";
            if (total > maxVisible) {
                hint += " §e(" + (this.selectedIndex + 1) + "/" + total + ")";
            }
            int hintY = boxTop - 12;
            guiGraphics.drawString(mc.font, hint, x + 2, hintY, 0xAAAAAA, false);
        }
    }
}
