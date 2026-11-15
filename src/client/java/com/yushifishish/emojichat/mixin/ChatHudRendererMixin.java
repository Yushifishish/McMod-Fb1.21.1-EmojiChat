package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiGraphics.class)
public class ChatHudRendererMixin {

    @Redirect(
        method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I"
        )
    )
    private int redirectDrawString(Font font, String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLight) {
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String emojiName = matcher.group(1);
            if (EmojiTextureManager.has(emojiName)) {
                GuiGraphics graphics = (GuiGraphics) (Object) this;
                int imageSize = 96;
                int imgX = (int) x;
                int imgY = (int) (y - imageSize + font.lineHeight);
                
                graphics.blit(EmojiTextureManager.getTexture(emojiName), imgX, imgY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imgX, imgY, imageSize, imageSize));
                
                return imageSize; 
            }
        }
        return font.drawInBatch(text, x, y, color, dropShadow, matrix, bufferSource, displayMode, backgroundColor, packedLight);
    }
}
