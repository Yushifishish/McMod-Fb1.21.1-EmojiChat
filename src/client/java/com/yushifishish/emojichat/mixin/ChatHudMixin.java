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
        // Base Y coordinate for chat (approximate bottom)
        int baseY = mc.getWindow().getGuiScaledHeight() - 40;

        // Iterate through visible chat lines
        for (int i = 0; i < trimmedMessages.size(); i++) {
            Object line = trimmedMessages.get(i);
            if (line == null) continue;

            try {
                // Use reflection to safely get the text content of the chat line
                java.lang.reflect.Method getContentMethod = line.getClass().getMethod("content");
                Object contentObj = getContentMethod.invoke(line);
                String text = contentObj.toString();

                // Find emoji pattern
                Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    String emojiName = matcher.group(1);
                    if (EmojiTextureManager.has(emojiName)) {
                        // Calculate the Y coordinate for this specific chat line
                        // Since we added 10 newlines, this line is now near the top of the expanded block
                        int textY = baseY - (i * 9);

                        // Draw the image below the text. Image size is 96x96.
                        int imageSize = 96;
                        int imageX = 4; // Standard chat left padding
                        
                        // Position the image to fill the empty lines we created
                        int imageY = textY - imageSize + 9;

                        // Render the uncompresssed image stretched to 96x96
                        if (EmojiTextureManager.getTexture(emojiName) != null) {
                            guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), imageX, imageY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                        }

                        // Register click area for preview and save
                        EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imageX, imageY, imageSize, imageSize));
                    }
                }
            } catch (Exception e) {
                // Ignore reflection errors to prevent crashes
            }
        }
    }
}
