package com.yushifishish.emojichat.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EmojiPreviewScreen extends Screen {
    private final ResourceLocation texture;
    private final String emojiName;

    public EmojiPreviewScreen(String name, ResourceLocation texture) {
        super(Component.literal("Preview: " + name));
        this.emojiName = name;
        this.texture = texture;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        
        // Calculate size to fit screen but keep aspect ratio
        int maxWidth = this.width - 40;
        int maxHeight = this.height - 40;
        
        // Draw the image centered
        guiGraphics.blit(texture, (this.width - 200) / 2, (this.height - 200) / 2, 0, 0, 200, 200, 200, 200);
        
        guiGraphics.drawCenteredString(this.font, "Name: " + emojiName, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Press ESC to close", this.width / 2, this.height - 30, 0xAAAAAA);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
