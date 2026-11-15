package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public class ChatHudMixin {

    @Shadow private List<?> trimmedMessages;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int tickDelta, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        EmojiClickData.CLICK_AREAS.clear();
        if (trimmedMessages == null || trimmedMessages.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int baseY = mc.getWindow().getGuiScaledHeight() - 40;
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");

        for (int i = 0; i < trimmedMessages.size(); i++) {
            Object line = trimmedMessages.get(i);
            if (line == null) continue;

            try {
                java.lang.reflect.Method getContentMethod = line.getClass().getMethod("content");
                String text = getContentMethod.invoke(line).toString();

                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String emojiName = matcher.group(1);
                    if (EmojiTextureManager.has(emojiName)) {
                        int imageSize = 96;
                        int imageX = 4;
                        
                        // 修正Y坐标：图片应该画在文字行的“下方”
                        // textY 是文字行在屏幕上的Y坐标
                        int textY = baseY - (i * 9); 
                        // imageY 是图片的顶部Y坐标。我们让图片顶部与文字行底部对齐
                        int imageY = textY + 9; 

                        if (EmojiTextureManager.getTexture(emojiName) != null) {
                            guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), imageX, imageY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                        }
                        EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imageX, imageY, imageSize, imageSize));
                    }
                }
            } catch (Exception e) { /* ignore */ }
        }
    }
}
