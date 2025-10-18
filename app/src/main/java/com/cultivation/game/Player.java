package com.cultivation.game;

import android.content.*;

public class Player {
    public String name;
    public int level;
    public int health;
    public int maxHealth;
    public int qi;
    public int maxQi;
    public int power;
    public int experience;
    public double cultivationStage;
    public int deathCount;
    public int x, y;

    // Дополнительные характеристики
    public int physicalRank;
    public int spiritualRank;
    public int combatRank;

    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.maxHealth = 100;
        this.health = maxHealth;
        this.maxQi = 50;
        this.qi = maxQi;
        this.power = 10;
        this.experience = 0;
        this.cultivationStage = 1.0;
        this.deathCount = 0;
        this.x = 500;
        this.y = 300;

        this.physicalRank = 10;
        this.spiritualRank = 10;
        this.combatRank = 10;
    }

    public int getTotalRank() {
        return (physicalRank + spiritualRank + combatRank) / 3;
    }

    public String getRankTitle() {
        int total = getTotalRank();
        if (total < 20) return "Новичок";
        if (total < 40) return "Ученик";
        if (total < 60) return "Воин";
        if (total < 80) return "Мастер";
        if (total < 100) return "Просветленный";
        if (total < 150) return "Бессмертный";
        if (total < 200) return "Небесный";
        return "Владыка";
    }
}
