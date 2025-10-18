package com.cultivation.game;

public class Resource extends Entity {
    public String type;
    public int quality;
    public String source;

    public Resource(String type, int quality, int x, int y) {
        super(x, y);
        this.type = type;
        this.quality = quality;
        this.source = "world";
    }

    public Resource(String type, int quality, int x, int y, String source) {
        super(x, y);
        this.type = type;
        this.quality = quality;
        this.source = source;
    }

    public boolean isHealingType() {
        return "Небесная Роса".equals(type) || "Корень Жизни".equals(type);
    }

    public boolean isMonsterDrop() {
        return "monster".equals(source);
    }

    public int getBonusValue() {
        return quality * (isMonsterDrop() ? 15 : 10);
    }

    public String getQualityStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < quality; i++) {
            stars.append("★");
        }
        return stars.toString();
    }
}
