package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiClickArea;
import com.yushifishish.emojichat.client.EmojiTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @Shadow private int getLineHeight() { return 0; }

    public static List<EmojiClickArea> CLICK_AREAS = new ArrayList<>();

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int tickDelta, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        CLICK_AREAS.clear();
        
        Minecraft mc = Minecraft.getInstance();
        // Scan visible messages for emojis
        // Note: In 1.21.1, ChatComponent has a private list. For compatibility, we do a simplified scan of recent messages.
        // Because manipulating lines is complex, we render the images anchored above the chat box area.
        
        // We anchor the image preview on the left side of the chat area.
        int yOffset = mc.getWindow().getGuiScaledHeight() - 40;
        
        for (String name : EmojiTextureManager.getAllNames()) {
            // Simple check to render one test image to prove functionality if chat matching is hard.
            // In production, we would iterate over `ChatComponent.allMessages` or `trimmedMessages`.
            if (mc.gui.getChat().getRecentChat().stream().anyMatch(line -> line.getString().contains("[" + name + "]"))) {
                
                int size = 128; // Render at 128x128 maximum limit
                int x = 4;
                int y = yOffset - size; // Above the input box
                
                // Draw background
                guiGraphics.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0x80000000);
                // Draw image
                if (EmojiTextureManager.getLarge(name) != null) {
                    guiGraphics.blit(EmojiTextureManager.getLarge(name), x, y, 0, 0, size, size, size, size);
                }
                
                // Record click area for ChatScreenMixin
                CLICK_AREAS.add(new EmojiClickArea(name, x, y, size, size));
                
                // Only render one large image per frame for clarity (or offset them)
                break; 
            }
        }
    }
}
