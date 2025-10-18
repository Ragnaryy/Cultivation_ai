package com.cultivation.game;

public class Entity {
    public Vector2 position;

    public Entity() {
        this.position = new Vector2();
    }

    public Entity(int x, int y) {
        this.position = new Vector2(x, y);
    }
}
