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

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.input.setResponder(this::updateSuggestions);
    }

    @Unique
    private void updateSuggestions(String text) {
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
                    }
                    return;
                }
            }
        }
        
        this.showSuggestions = false;
        this.openBracketIndex = -1;
        this.selectedIndex = 0;
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.showSuggestions && currentMatches != null && !currentMatches.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.selectedIndex = (this.selectedIndex + 1) % currentMatches.size();
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                this.selectedIndex = (this.selectedIndex - 1 + currentMatches.size()) % currentMatches.size();
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
            if (verticalAmount > 0) {
                this.selectedIndex = (this.selectedIndex - 1 + currentMatches.size()) % currentMatches.size();
            } else if (verticalAmount < 0) {
                this.selectedIndex = (this.selectedIndex + 1) % currentMatches.size();
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.showSuggestions && currentMatches != null && !currentMatches.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            
            // Calculate X position based on cursor location
            String text = this.input.getValue();
            int cursorPos = this.input.getCursorPosition();
            // Get text width before the cursor
            int textWidthBeforeCursor = mc.font.width(text.substring(0, Math.min(cursorPos, text.length())));
            
            int menuWidth = 120;
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            
            // The X position of the cursor on screen
            int cursorX = this.input.getX() + textWidthBeforeCursor;
            
            // Clamp the menu to fit within the screen bounds
            int x = Math.max(2, Math.min(screenWidth - menuWidth - 2, cursorX));
            
            // Y position remains relative to the input box (above it)
            int baseY = this.input.getY() - 28;

            // Calculate scroll window
            int total = currentMatches.size();
            int maxVisible = 5; 
            int startIndex = 0;
            
            if (this.selectedIndex >= maxVisible) {
                startIndex = this.selectedIndex - maxVisible + 1;
            }
            if (startIndex > total - maxVisible) {
                startIndex = Math.max(0, total - maxVisible);
            }
            
            int endIndex = Math.min(total, startIndex + maxVisible);
            int displayCount = endIndex - startIndex;

            for (int i = startIndex; i < endIndex; i++) {
                String name = currentMatches.get(i);
                int displayIndex = i - startIndex; 
                int y = baseY - (displayIndex * 22);
                
                if (i == this.selectedIndex) {
                    guiGraphics.fill(x, y - 2, x + menuWidth, y + 20, 0x80FFFFFF);
                } else {
                    guiGraphics.fill(x, y - 2, x + menuWidth, y + 20, 0x80000000);
                }
                
                if (EmojiTextureManager.has(name)) {
                    guiGraphics.blit(EmojiTextureManager.get(name), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
                }
                
                guiGraphics.drawString(mc.font, "[" + name + "]", x + 24, y + 6, 0xFFFFFF, false);
            }
            
            // Bottom hint and page indicator
            String hint = "§7Use UP/DOWN or SCROLL, TAB to complete";
            if (total > maxVisible) {
                hint += " §e(" + (this.selectedIndex + 1) + "/" + total + ")";
            }
            guiGraphics.drawString(mc.font, hint, x + 2, baseY - (displayCount * 22) - 10, 0xAAAAAA, false);
        }
    }
}
