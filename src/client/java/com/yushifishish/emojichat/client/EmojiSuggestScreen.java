package com.yushifishish.emojichat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EmojiSuggestScreen extends Screen {
    private final ChatScreen parent;
    private final String currentText;

    public EmojiSuggestScreen(ChatScreen parent, String currentText) {
        super(Component.literal("Emoji Suggest"));
        this.parent = parent;
        this.currentText = currentText;
    }

    @Override
    protected void init() {
        // 在屏幕中间添加一个按钮
        this.addRenderableWidget(Button.builder(Component.literal("test"), b -> {
            // 点击后，回到聊天框并补全
            String newText = currentText + "test]";
            this.parent.insertText(newText, false); // 把文字塞回去
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(this.width / 2 - 40, this.height / 2 - 10, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        
        // 在按钮左边画那个红色测试图片
        if (EmojiTextureManager.has("test")) {
            guiGraphics.blit(EmojiTextureManager.get("test"), 
                this.width / 2 - 70, this.height / 2 - 10, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
