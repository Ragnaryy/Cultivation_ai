package com.cultivation.game;

public class Monster {
    public String type;
    public int level;
    public int health;
    public int maxHealth;
    public int power;
    public int x, y;

    public Monster(String type, int level, int x, int y) {
        this.type = type;
        this.level = level;
        this.maxHealth = 40 + (level * 20);
        this.health = maxHealth;
        this.power = 8 + (level * 4);
        this.x = x;
        this.y = y;
    }
}
