package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
import com.yushifishish.emojichat.client.EmojiRenderCache;
import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int tickDelta, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        EmojiClickData.CLICK_AREAS.clear();
        
        // Draw only if we have a cached emoji from the message
        if (EmojiRenderCache.LAST_EMOJI != null && EmojiTextureManager.has(EmojiRenderCache.LAST_EMOJI)) {
            String emojiName = EmojiRenderCache.LAST_EMOJI;
            
            Minecraft mc = Minecraft.getInstance();
            double chatScale = mc.options.chatScale().get();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            int imageSize = 96;
            int imageX = 4;
            
            // Calculate position above the chat box (The input box is about 40 pixels from bottom)
            // The text is at the bottom, the empty lines are above it.
            // So we push the image up by 96 pixels.
            int imageY = screenHeight - 40 - imageSize - 10;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, (float)(screenHeight - 40), 0.0F);
            guiGraphics.pose().scale((float) chatScale, (float) chatScale, 1.0F);
            
            // The image Y here is relative to the chat box bottom
            int relativeY = -imageSize - 15;
            
            if (EmojiTextureManager.getTexture(emojiName) != null) {
                guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), imageX, relativeY, 0, 0, imageSize, imageSize, imageSize, imageSize);
            }
            
            guiGraphics.pose().popPose();

            // Click coordinates (absolute)
            int screenX = (int)(imageX * chatScale);
            int screenY = (int)((screenHeight - 40) + relativeY * chatScale);
            
            EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, screenX, screenY, (int)(imageSize * chatScale), (int)(imageSize * chatScale)));
        }
    }
}
