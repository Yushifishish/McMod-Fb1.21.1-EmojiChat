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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiGraphics.class)
public class ChatHudRendererMixin {

    @Inject(
        method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onDrawString(Font font, String text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String emojiName = matcher.group(1);
            if (EmojiTextureManager.has(emojiName)) {
                GuiGraphics graphics = (GuiGraphics) (Object) this;
                int imageSize = 96;
                int imgX = x;
                int imgY = y - imageSize + font.lineHeight;
                
                graphics.blit(EmojiTextureManager.getTexture(emojiName), imgX, imgY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imgX, imgY, imageSize, imageSize));
                
                cir.setReturnValue(imageSize); 
            }
        }
    }
}
