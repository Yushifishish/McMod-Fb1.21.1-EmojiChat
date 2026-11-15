package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.EmojiChat;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected EditBox input;

    @Unique private boolean showMenu = false;
    @Unique private List<String> matches;
    @Unique private int selectedIndex = 0;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        String text = this.input.getValue();
        if (text.startsWith("/")) { showMenu = false; return; }

        int lastOpen = text.lastIndexOf('[');
        if (lastOpen != -1 && text.indexOf(']', lastOpen) == -1) {
            String query = text.substring(lastOpen + 1).toLowerCase();
            matches = EmojiTextureManager.getAllNames().stream()
                    .filter(name -> name.toLowerCase().contains(query))
                    .collect(Collectors.toList());
            if (!matches.isEmpty()) {
                showMenu = true;
                if (selectedIndex >= matches.size()) selectedIndex = 0;
            } else { showMenu = false; }
        } else { showMenu = false; }

        if (showMenu && matches != null && !matches.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                selectedIndex = (selectedIndex + 1) % matches.size();
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_UP) {
                selectedIndex = (selectedIndex - 1 + matches.size()) % matches.size();
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER) {
                input.setValue(text.substring(0, lastOpen) + "[" + matches.get(selectedIndex) + "]");
                showMenu = false;
                cir.setReturnValue(true);
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                showMenu = false;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!showMenu || matches == null || matches.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        int x = input.getX();
        int baseY = input.getY() - 28;
        for (int i = 0; i < Math.min(matches.size(), 5); i++) {
            String name = matches.get(i);
            int y = baseY - (i * 22);
            guiGraphics.fill(x, y - 2, x + 120, y + 20, i == selectedIndex ? 0x80FFFFFF : 0x80000000);
            guiGraphics.blit(EmojiTextureManager.getTexture(name), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
            guiGraphics.drawString(mc.font, "[" + name + "]", x + 24, y + 6, 0xFFFFFF, false);
        }
    }
}
