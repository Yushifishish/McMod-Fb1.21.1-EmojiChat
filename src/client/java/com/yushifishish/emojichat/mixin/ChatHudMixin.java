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
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");

        // Apply the exact same PoseStack transform as vanilla ChatHud
        double chatScale = mc.options.chatScale().get();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        guiGraphics.pose().pushPose();
        // 1. Translate to the bottom chat area (40px from bottom)
        guiGraphics.pose().translate(0.0F, (float)(screenHeight - 40), 0.0F);
        // 2. Apply the user's chat scale
        guiGraphics.pose().scale((float) chatScale, (float) chatScale, 1.0F);
        // 3. Move up by the line height to start rendering the first line
        guiGraphics.pose().translate(0.0F, -8.0F, 0.0F);

        for (int i = 0; i < trimmedMessages.size(); i++) {
            Object line = trimmedMessages.get(i);
            if (line == null) continue;

            try {
                java.lang.reflect.Method getContentMethod = line.getClass().getMethod("content");
                Object contentObj = getContentMethod.invoke(line);

                if (contentObj instanceof Component comp) {
                    String text = comp.getString();
                    Matcher matcher = pattern.matcher(text);

                    if (matcher.find()) {
                        String emojiName = matcher.group(1);
                        if (EmojiTextureManager.has(emojiName)) {
                            int imageSize = 96;
                            int imageX = 4;
                            
                            // Since the text is at index 'i', and the blank lines are added AFTER the text,
                            // visually, the blank lines are at smaller indices (below the text).
                            // We draw the image directly below the text line.
                            // Note: Y axis grows downwards.
                            int textY = -(i * 9); // Relative to the first line
                            
                            // The image starts just below the text line
                            int imageY = textY + 9; 

                            if (EmojiTextureManager.getTexture(emojiName) != null) {
                                // Render the image (it will be scaled by the PoseStack automatically)
                                guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), imageX, imageY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                            }
                            
                            // Save click area (need to convert back to absolute screen coordinates)
                            int absoluteX = (int)(imageX * chatScale);
                            int absoluteY = (int)((screenHeight - 40 + (imageY - 8) * chatScale));
                            
                            EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, absoluteX, absoluteY, (int)(imageSize * chatScale), (int)(imageSize * chatScale)));
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        
        guiGraphics.pose().popPose();
    }
}
