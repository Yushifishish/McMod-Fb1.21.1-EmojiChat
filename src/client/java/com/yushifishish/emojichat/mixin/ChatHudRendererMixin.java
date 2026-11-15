package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiRenderCache;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
    @Shadow @Final private TextRenderer textRenderer;
    @Shadow private int scrolledLines;

    // 假设你的表情标记格式为 :emoji_name: 或 [emoji:name]
    private static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+):|\\[emoji:([a-zA-Z0-9_]+)\\]");

    // 原版聊天行高常量（1.21.1 中为 9）
    private static final int LINE_HEIGHT = 9;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int tickDelta, boolean focused, CallbackInfo ci) {
        // 仅在聊天界面打开时叠加渲染，避免在普通 HUD 上错误绘制
        if (!focused) return;

        int screenHeight = context.getScaledWindowHeight();
        // 原版聊天框的底部 Y 坐标（根据 1.21.1 的布局推算）
        int bottomY = screenHeight - 40; // 预留输入框高度
        int xStart = 4; // 聊天文本左侧 padding

        for (int i = 0; i < visibleMessages.size(); i++) {
            ChatHudLine.Visible line = visibleMessages.get(i);
            OrderedText content = line.content();

            // 计算该行在屏幕上的 Y 坐标
            int lineY = bottomY - (i * LINE_HEIGHT) - LINE_HEIGHT;
            if (lineY < 0) break; // 超出屏幕顶部，停止

            String plainText = content.getString(); // 注意：OrderedText 到 String 会丢失样式，但用于定位足够
            Matcher matcher = EMOJI_PATTERN.matcher(plainText);

            int charX = xStart;
            int lastEnd = 0;

            while (matcher.find()) {
                // 计算标记之前的文本宽度，累加到 X 坐标
                String before = plainText.substring(lastEnd, matcher.start());
                charX += textRenderer.getWidth(before);

                // 提取表情名称
                String emojiName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                Identifier texture = EmojiRenderCache.getTexture(emojiName);

                if (texture != null) {
                    // 表情尺寸与一个字符高度对齐（9x9），宽度可按纹理比例缩放，这里简化为正方形
                    int size = LINE_HEIGHT;
                    context.drawTexture(texture, charX, lineY, 0, 0, size, size, size, size);
                }

                // 标记本身的宽度也要跳过（这里简化处理，按替换后的图片宽度算）
                charX += LINE_HEIGHT;
                lastEnd = matcher.end();
            }
        }
    }
}
