package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiRenderCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected EditBox chatField;

    private boolean showEmojiMenu = false;
    private List<String> currentMatches = new ArrayList<>();
    private int menuX, menuY;
    private int menuWidth, menuHeight;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String text = chatField.getValue();
        int cursor = chatField.getCursorPosition();

        int lastOpen = text.lastIndexOf('[', cursor - 1);
        if (lastOpen == -1) { showEmojiMenu = false; return; }

        String between = text.substring(lastOpen + 1, cursor);
        if (between.contains("]")) { showEmojiMenu = false; return; }

        currentMatches = EmojiRenderCache.getAllEmojiNames().stream()
                .filter(name -> name.startsWith(between))
                .limit(8)
                .toList();

        if (currentMatches.isEmpty()) { showEmojiMenu = false; return; }

        showEmojiMenu = true;
        menuWidth = 120;
        menuHeight = currentMatches.size() * 12 + 4;
        menuX = chatField.getX();
        menuY = chatField.getY() - menuHeight - 2;

        guiGraphics.fill(menuX - 2, menuY - 2, menuX + menuWidth + 2, menuY + menuHeight + 2, 0xCC000000);
        guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0xFFFFFFFF);

        for (int i = 0; i < currentMatches.size(); i++) {
            String name = currentMatches.get(i);
            int itemY = menuY + i * 12 + 2;
            boolean hovered = mouseX >= menuX && mouseX < menuX + menuWidth
                    && mouseY >= itemY && mouseY < itemY + 12;

            int color = hovered ? 0xFFFFD700 : 0xFFFFFFFF;
            guiGraphics.drawString(Minecraft.getInstance().font, name, menuX + 4, itemY, color, false);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!showEmojiMenu || button != 0) return;

        for (int i = 0; i < currentMatches.size(); i++) {
            int itemY = menuY + i * 12 + 2;
            if (mouseX >= menuX && mouseX < menuX + menuWidth
                    && mouseY >= itemY && mouseY < itemY + 12) {

                String selected = currentMatches.get(i);
                String text = chatField.getValue();
                int cursor = chatField.getCursorPosition();
                int lastOpen = text.lastIndexOf('[', cursor - 1);

                String before = text.substring(0, lastOpen);
                String after = text.substring(cursor);
                String replacement = "[emoji:" + selected + "]";
                chatField.setValue(before + replacement + after);
                chatField.setCursorPosition((before + replacement).length());
                showEmojiMenu = false;
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
