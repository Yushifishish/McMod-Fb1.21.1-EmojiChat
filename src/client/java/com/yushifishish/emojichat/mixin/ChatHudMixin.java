package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.EmojiChat;
import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
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
        if (!EmojiTextureManager.getAllNames().isEmpty()) {
            String testName = EmojiTextureManager.getAllNames().get(0);
            int centerX = mc.getWindow().getGuiScaledWidth() / 2;
            int centerY = mc.getWindow().getGuiScaledHeight() / 2;
            
            guiGraphics.fill(centerX - 50, centerY - 50, centerX + 50, centerY + 50, 0xFFFF0000); // 红块
            guiGraphics.drawString(mc.font, "TEST: " + testName, centerX - 40, centerY - 10, 0xFFFFFFFF);
            
            if (EmojiTextureManager.getTexture(testName) != null) {
                // 渲染一下第一张图，确信图没坏
                guiGraphics.blit(EmojiTextureManager.getTexture(testName), centerX - 48, centerY - 40, 0, 0, 96, 96, 96, 96);
            }
        }

        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
        int baseY = mc.getWindow().getGuiScaledHeight() - 40;
        double chatScale = mc.options.chatScale().get();
        int lineHeight = (int) (9 * chatScale);

        for (int i = 0; i < trimmedMessages.size(); i++) {
            Object line = trimmedMessages.get(i);
            if (line == null) continue;

            try {
                java.lang.reflect.Method getContentMethod = line.getClass().getMethod("content");
                Object contentObj = getContentMethod.invoke(line);
                
                String text = "";
                // 必须用 getString() 才能拿到纯文本，不能用 toString()
                if (contentObj instanceof Component comp) {
                    text = comp.getString();
                }
                
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String emojiName = matcher.group(1);
                    EmojiChat.LOGGER.info("CHAT RENDER DEBUG: Found emoji text! name=" + emojiName + " at index=" + i);
                    
                    if (EmojiTextureManager.has(emojiName)) {
                        int imageSize = 96;
                        int imageX = 4;
                        
                        // Y坐标计算：文字行在底部，图片画在它下方
                        int textY = baseY - (i * lineHeight);
                        int imageY = textY + lineHeight;

                        if (EmojiTextureManager.getTexture(emojiName) != null) {
                            guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), imageX, imageY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                            EmojiChat.LOGGER.info("CHAT RENDER DEBUG: Drew image at X=" + imageX + " Y=" + imageY);
                        }
                        EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imageX, imageY, imageSize, imageSize));
                    }
                }
            } catch (Exception e) {
                EmojiChat.LOGGER.error("CHAT RENDER ERROR", e);
            }
        }
    }
}
