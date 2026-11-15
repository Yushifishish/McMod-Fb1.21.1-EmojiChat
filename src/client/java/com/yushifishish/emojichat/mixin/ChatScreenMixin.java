package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiRenderCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
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

    @Shadow
    protected TextFieldWidget chatField;

    private boolean showEmojiMenu = false;
    private List<String> currentMatches = new ArrayList<>();
    private int menuX, menuY;
    private int menuWidth, menuHeight;

    /** 每一帧实时检查输入框内容，不依赖 keyPressed，避免原版提前 return 导致逻辑丢失 */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String text = chatField.getText();
        int cursor = chatField.getCursor();

        // 查找光标前最后一个未闭合的 '['
        int lastOpen = text.lastIndexOf('[', cursor - 1);
        if (lastOpen == -1) {
            showEmojiMenu = false;
            return;
        }
        String between = text.substring(lastOpen + 1, cursor);
        if (between.contains("]")) {
            showEmojiMenu = false;
            return;
        }

        // 根据 '[' 之后到光标之间的内容，过滤表情名
        currentMatches = EmojiRenderCache.getAllEmojiNames().stream()
                .filter(name -> name.startsWith(between))
                .limit(8)
                .toList();

        if (currentMatches.isEmpty()) {
            showEmojiMenu = false;
            return;
        }

        showEmojiMenu = true;

        // 菜单尺寸与位置：紧贴输入框上方，宽度固定 120，高度按条目数计算
        menuWidth = 120;
        menuHeight = currentMatches.size() * 12 + 4;
        menuX = chatField.getX();
        menuY = chatField.getY() - menuHeight - 2;

        // 绘制背景（深色半透明）与边框
        context.fill(menuX - 2, menuY - 2, menuX + menuWidth + 2, menuY + menuHeight + 2, 0xCC000000);
        context.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0xFFFFFFFF);

        // 绘制候选列表，高亮鼠标悬停项
        for (int i = 0; i < currentMatches.size(); i++) {
            String name = currentMatches.get(i);
            int itemY = menuY + i * 12 + 2;
            boolean hovered = mouseX >= menuX && mouseX < menuX + menuWidth
                    && mouseY >= itemY && mouseY < itemY + 12;

            int color = hovered ? 0xFFFFD700 : 0xFFFFFFFF; // 悬停金色，否则白色
            context.drawTextWithShadow(
                    MinecraftClient.getInstance().textRenderer,
                    name,
                    menuX + 4,
                    itemY,
                    color
            );
        }
    }

    /** 鼠标点击菜单项时，将完整表情标记插入输入框，替换掉未闭合的 '[' 前缀 */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!showEmojiMenu || button != 0) return;

        for (int i = 0; i < currentMatches.size(); i++) {
            int itemY = menuY + i * 12 + 2;
            if (mouseX >= menuX && mouseX < menuX + menuWidth
                    && mouseY >= itemY && mouseY < itemY + 12) {

                String selected = currentMatches.get(i);
                String text = chatField.getText();
                int cursor = chatField.getCursor();
                int lastOpen = text.lastIndexOf('[', cursor - 1);

                // 用完整标记替换掉 "[前缀"
                String before = text.substring(0, lastOpen);
                String after = text.substring(cursor);
                String replacement = "[emoji:" + selected + "]";
                chatField.setText(before + replacement + after);

                // 光标移到标记之后
                chatField.setCursor((before + replacement).length());
                showEmojiMenu = false;
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
