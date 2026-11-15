package com.yushifishish.emojichat.mixin;

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

import java.lang.reflect.Field;
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
        double chatScale = mc.options.chatScale().get();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");

        for (int i = 0; i < trimmedMessages.size(); i++) {
            Object lineObj = trimmedMessages.get(i);
            if (lineObj == null) continue;

            String lineText = "";
            
            // SAFE REFLECTION: Loop through fields to find the Component text (avoids missing method errors)
            try {
                if (lineObj instanceof Component comp) {
                    lineText = comp.getString();
                } else {
                    for (Field field : lineObj.getClass().getDeclaredFields()) {
                        if (Component.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            Component comp = (Component) field.get(lineObj);
                            if (comp != null) {
                                lineText = comp.getString();
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

            Matcher matcher = pattern.matcher(lineText);
            if (matcher.find()) {
                String emojiName = matcher.group(1);
                if (EmojiTextureManager.has(emojiName)) {
                    int imageSize = 96;

                    // Render context setup
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, (float)(screenHeight - 40), 0.0F);
                    guiGraphics.pose().scale((float) chatScale, (float) chatScale, 1.0F);
                    
                    // Since index 0 is the bottom, we draw the image covering the empty lines above the text
                    int relativeY = -(i * 9) - 9 - imageSize;
                    
                    if (EmojiTextureManager.getTexture(emojiName) != null) {
                        guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), 4, relativeY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                    }
                    
                    guiGraphics.pose().popPose();

                    // Absolute click coords
                    int screenX = (int)(4 * chatScale);
                    int screenY = (int)((screenHeight - 40) + relativeY * chatScale);
                    EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, screenX, screenY, (int)(imageSize * chatScale), (int)(imageSize * chatScale)));
                }
            }
        }
    }
}
