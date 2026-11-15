package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiChatClient;
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

    // --- 键盘交互逻辑（保持不变）---
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        String text = this.input.getValue();
        // ✅ 核心：如果处于指令状态，直接放行
        if (EmojiChatClient.isCommandState) {
            this.showSuggestions = false;
            return; 
        }
        // ... (后续的上下键、Tab、回车逻辑保持不变) ...
    }

    // --- 渲染与实时检测 ---
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String text = this.input.getValue();

        // ✅ 核心：如果处于指令状态，强制关闭表情菜单
        if (EmojiChatClient.isCommandState) {
            this.showSuggestions = false;
        } else {
            // ... (原有的实时检测逻辑保持不变) ...
        }
        // ... (后续的渲染逻辑保持不变) ...
    }
}
