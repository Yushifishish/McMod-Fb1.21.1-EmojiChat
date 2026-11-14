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
        // 👇 懒加载核心：只有在这个界面被打开（init）的时候，才去生成红色测试图
        // 此时游戏早已跑起来，图形环境 1000% 是就绪的
        if (!EmojiTextureManager.has("test")) {
            EmojiTextureManager.putTest();
        }

        this.addRenderableWidget(Button.builder(Component.literal("test"), b -> {
            String newText = currentText + "test]";
            this.parent.insertText(newText, false); 
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(this.width / 2 - 40, this.height / 2 - 10, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        
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
