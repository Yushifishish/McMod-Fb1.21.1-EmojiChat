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
        
        // 只要有拦截到表情，就在固定位置画图
        if (EmojiRenderCache.LAST_EMOJI != null && EmojiTextureManager.has(EmojiRenderCache.LAST_EMOJI)) {
            String emojiName = EmojiRenderCache.LAST_EMOJI;
            
            // 画在左侧偏下的位置
            int imageX = 4;
            int imageY = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 200;
            int imageSize = 96;

            if (EmojiTextureManager.getTexture(emojiName) != null) {
                guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), imageX, imageY, 0, 0, imageSize, imageSize, imageSize, imageSize);
            }
            
            EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imageX, imageY, imageSize, imageSize));
        }
    }
}
