package com.yushifishish.emojichat.client;

public class EmojiClickArea {
    public final String name;
    public final double x, y, width, height;

    public EmojiClickArea(String name, double x, double y, double width, double height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
