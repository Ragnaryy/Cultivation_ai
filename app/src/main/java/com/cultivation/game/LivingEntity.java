package com.cultivation.game;

public class LivingEntity extends Entity {
    public int health;
    public int maxHealth;
    public int power;
    public int level;

    public LivingEntity() {
        super();
    }

    public LivingEntity(int x, int y) {
        super(x, y);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
}
