package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiGraphics.class)
public class ChatHudRendererMixin {

    @Redirect(
        method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
    )
    private int redirectDrawString(net.minecraft.client.gui.Font font, String text, float x, float y, int color, boolean dropShadow, org.joml.Matrix4f matrix, net.minecraft.client.gui.Font.DisplayMode displayMode, int backgroundColor, int packedLight) {
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String emojiName = matcher.group(1);
            if (EmojiTextureManager.has(emojiName)) {
                GuiGraphics graphics = (GuiGraphics) (Object) this;
                int imageSize = 96; // 图片显示尺寸
                int imgX = (int) x;
                int imgY = (int) (y - imageSize + font.lineHeight); // 对齐基线
                
                graphics.blit(EmojiTextureManager.getTexture(emojiName), imgX, imgY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imgX, imgY, imageSize, imageSize));
                
                // 返回图片宽度，让后续文字正确排版
                return imageSize; 
            }
        }
        // 正常绘制文字
        return font.drawInBatch(text, x, y, color, dropShadow, matrix, displayMode, backgroundColor, packedLight);
    }
}
