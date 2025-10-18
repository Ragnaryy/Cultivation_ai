package com.cultivation.game;

public class Vector2 {
    public int x, y;

    public Vector2() {
        this(0, 0);
    }

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Vector2 other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    public void moveTowards(Vector2 target, int speed) {
        int dx = target.x - x;
        int dy = target.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            x += (int)(dx / length * speed);
            y += (int)(dy / length * speed);
        }
    }

    public void clamp(int minX, int minY, int maxX, int maxY) {
        x = Math.max(minX, Math.min(maxX, x));
        y = Math.max(minY, Math.min(maxY, y));
    }
}
