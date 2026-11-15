package com.yushifishish.emojichat.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EmojiPreviewScreen extends Screen {
    private final String emojiName;

    public EmojiPreviewScreen(String name) {
        super(Component.literal("Preview: " + name));
        this.emojiName = name;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        
        // Draw original image texture (Note: We use the large texture here, but rendered at 100% scale)
        // Since MC UI uses scaling, we render it as big as possible while fitting the screen.
        int maxSize = Math.min(this.width, this.height) - 40;
        ResourceLocation tex = EmojiTextureManager.getLarge(emojiName);
        
        if (tex != null) {
            guiGraphics.blit(tex, (this.width - maxSize) / 2, (this.height - maxSize) / 2, 0, 0, maxSize, maxSize, maxSize, maxSize);
        }
        
        guiGraphics.drawCenteredString(this.font, "Name: " + emojiName, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Press ESC to close", this.width / 2, this.height - 30, 0xAAAAAA);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
