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
        // 懒加载测试表情
        if (!EmojiTextureManager.has("test")) EmojiTextureManager.putTest();
        if (!EmojiTextureManager.has("test2")) EmojiTextureManager.putTest2();

        // 兼容半角 [ 和全角 【
        int lastOpenHalf = text.lastIndexOf('[');
        int lastOpenFull = text.lastIndexOf('【');
        int lastOpen = Math.max(lastOpenHalf, lastOpenFull);

        if (lastOpen != -1) {
            int closeHalf = text.indexOf(']', lastOpen);
            int closeFull = text.indexOf('】', lastOpen);
            boolean hasClose = (closeHalf != -1) || (closeFull != -1);

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

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.showSuggestions && currentMatches != null && !currentMatches.isEmpty()) {
            int x = 4;
            int baseY = this.input.getY() - 28;
            Minecraft mc = Minecraft.getInstance();

            int displayCount = Math.min(currentMatches.size(), 5);
            for (int i = 0; i < displayCount; i++) {
                String name = currentMatches.get(i);
                int y = baseY - (i * 22);
                
                if (i == this.selectedIndex) {
                    guiGraphics.fill(x, y - 2, x + 120, y + 20, 0x80FFFFFF);
                } else {
                    guiGraphics.fill(x, y - 2, x + 120, y + 20, 0x80000000);
                }
                
                if (EmojiTextureManager.has(name)) {
                    guiGraphics.blit(EmojiTextureManager.get(name), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
                }
                
                guiGraphics.drawString(mc.font, "[" + name + "]", x + 24, y + 6, 0xFFFFFF, false);
            }
            
            guiGraphics.drawString(mc.font, "§7Use UP/DOWN to select, TAB to complete", x + 2, baseY - (displayCount * 22) - 10, 0xAAAAAA, false);
        }
    }
}
