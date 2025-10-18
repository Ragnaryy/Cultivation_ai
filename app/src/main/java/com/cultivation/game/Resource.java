package com.cultivation.game;

public class Resource {
    public String type;
    public int quality; // 1-3
    public int x, y;

    public Resource(String type, int quality, int x, int y) {
        this.type = type;
        this.quality = quality;
        this.x = x;
        this.y = y;
    }
}
