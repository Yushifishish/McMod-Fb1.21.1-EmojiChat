package com.yushifishish.emojichat.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "emoji-chat")
public class EmojiChatConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean enableEmojiSuggestions = true; // 简单的开关示例

    @ConfigEntry.Gui.Tooltip
    public String customEmojiFolder = "config/emoji-chat/custom"; // 文件夹路径（可配置）
}
