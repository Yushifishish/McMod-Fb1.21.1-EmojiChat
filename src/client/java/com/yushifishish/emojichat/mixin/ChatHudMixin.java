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
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
        
        double chatScale = mc.options.chatScale().get();
        int bottomY = (int)(mc.getWindow().getGuiScaledHeight() - 40 * chatScale);
        int lineHeight = (int)(9 * chatScale);

        for (int i = 0; i < trimmedMessages.size(); i++) {
            Object lineObj = trimmedMessages.get(i);
            if (lineObj == null) continue;

            String lineText = "";
            try {
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
            } catch (Exception ignored) {}

            Matcher matcher = pattern.matcher(lineText);
            if (matcher.find()) {
                String emojiName = matcher.group(1);
                if (EmojiTextureManager.has(emojiName)) {
                    int imageSize = 96;
                    int imageX = 4;
                    
                    int textY = bottomY - (i * lineHeight);
                    int imageY = textY - imageSize + lineHeight;

                    if (EmojiTextureManager.getTexture(emojiName) != null) {
                        guiGraphics.blit(EmojiTextureManager.getTexture(emojiName), imageX, imageY, 0, 0, imageSize, imageSize, imageSize, imageSize);
                    }
                    
                    EmojiClickData.CLICK_AREAS.add(new EmojiClickArea(emojiName, imageX, imageY, imageSize, imageSize));
                }
            }
        }
    }
}
