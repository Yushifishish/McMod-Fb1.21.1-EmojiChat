package com.yushifishish.emojichat.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new Screen(Component.literal("Emoji Chat Settings")) {
            @Override protected void init() {
                this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                        .bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
            }
        };
    }
}
