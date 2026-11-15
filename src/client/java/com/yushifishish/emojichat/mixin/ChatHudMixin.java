package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiClickData;
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
        
        Minecraft mc = Minecraft.getInstance();
        int yOffset = mc.getWindow().getGuiScaledHeight() - 40;
        
        for (String name : EmojiTextureManager.getAllNames()) {
            if (EmojiTextureManager.has(name)) {
                int size = 128; 
                int x = 4;
                int y = yOffset - size; 
                
                guiGraphics.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0x80000000);
                if (EmojiTextureManager.getLarge(name) != null) {
                    guiGraphics.blit(EmojiTextureManager.getLarge(name), x, y, 0, 0, size, size, size, size);
                }
                
                EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(name, x, y, size, size));
                break; 
            }
        }
    }
}
