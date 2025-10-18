package com.cultivation.game;

public class Monster extends LivingEntity {
    public String type;
    public String element;
    public int rarity;
    public int cultivationStage;

    public Monster(String type, int level, int x, int y) {
        super(x, y);
        this.type = type;
        this.level = level;
        this.maxHealth = 40 + (level * 20);
        this.health = maxHealth;
        this.power = 8 + (level * 4);
        this.element = assignElement(type);
        this.rarity = calculateRarity();
        this.cultivationStage = Math.max(1, level / 10 + 1);
    }

    private String assignElement(String type) {
        if (type.contains("Огненный") || type.contains("Дракон")) return "Огонь";
        if (type.contains("Ледяной") || type.contains("Феникс")) return "Лед";
        if (type.contains("Громовой") || type.contains("Тигр")) return "Молния";
        if (type.contains("Земляной") || type.contains("Черепаха")) return "Земля";
        return "Без стихии";
    }

    private int calculateRarity() {
        double chance = Math.random();
        if (chance < 0.01) return 4;
        if (chance < 0.05) return 3;
        if (chance < 0.15) return 2;
        return 1;
    }

    public boolean isElemental() {
        return !"Без стихии".equals(element);
    }

    public String getRankTitle() {
        switch (cultivationStage) {
            case 1: return "Духовный зверь";
            case 2: return "Небесный зверь";
            case 3: return "Священный зверь";
            case 4: return "Древний зверь";
            case 5: return "Бессмертный зверь";
            default: return "Мифический зверь";
        }
    }

    public int getExpReward() {
        int baseExp = level * 20;
        return baseExp * rarity;
    }

    public boolean shouldDropResource() {
        return rarity >= 2 || Math.random() < 0.1;
    }

    public String getRarityName() {
        switch (rarity) {
            case 1: return "Обычный";
            case 2: return "Редкий";
            case 3: return "Элитный";
            case 4: return "Босс";
            default: return "Неизвестный";
        }
    }
}
