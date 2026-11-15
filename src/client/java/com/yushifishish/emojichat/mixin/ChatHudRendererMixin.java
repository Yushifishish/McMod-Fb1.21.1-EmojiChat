package com.yushifishish.emojichat.mixin;

import com.yushifishish.emojichat.client.EmojiRenderCache;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow @Final private List<GuiMessage.Line> trimmedMessages;
    @Shadow @Final private Font font;

    private static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+):|\\[emoji:([a-zA-Z0-9_]+)\\]");
    private static final int LINE_HEIGHT = 9;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(GuiGraphics guiGraphics, int tickDelta, boolean focused, CallbackInfo ci) {
        if (!focused) return;

        int screenHeight = guiGraphics.guiHeight();
        int bottomY = screenHeight - 40;
        int xStart = 4;

        for (int i = 0; i < trimmedMessages.size(); i++) {
            GuiMessage.Line line = trimmedMessages.get(i);
            String plainText = line.content().getString();

            int lineY = bottomY - (i * LINE_HEIGHT) - LINE_HEIGHT;
            if (lineY < 0) break;

            Matcher matcher = EMOJI_PATTERN.matcher(plainText);
            int charX = xStart;
            int lastEnd = 0;

            while (matcher.find()) {
                String before = plainText.substring(lastEnd, matcher.start());
                charX += font.width(before);

                String emojiName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                ResourceLocation texture = EmojiRenderCache.getTexture(emojiName);

                if (texture != null) {
                    int size = LINE_HEIGHT;
                    guiGraphics.blit(texture, charX, lineY, 0, 0, size, size, size, size);
                }

                charX += LINE_HEIGHT;
                lastEnd = matcher.end();
            }
        }
    }
}
