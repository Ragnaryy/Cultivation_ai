package com.cultivation.game;

import java.util.*;

public class WorldGenerator {
    private static final String[] MONSTER_TYPES = {
        "Огненный Дракон", "Ледяной Феникс", "Громовой Тигр", "Земляная Черепаха",
        "Небесный Змей", "Теневой Призрак", "Золотой Лев", "Хрустальный Феникс"
    };

    private static final String[] RESOURCE_TYPES = {
        "Небесная Роса", "Корень Жизни", "Ядро Дракона", "Слеза Феникса",
        "Звездная Пыль", "Корень Бессмертия", "Сердце Вселенной", "Семя Дао"
    };

    private Random random;
    private int worldWidth;
    private int worldHeight;

    public WorldGenerator(int worldWidth, int worldHeight) {
        this.random = new Random();
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public List<Monster> generateMonsters(int count, int cycle) {
        List<Monster> monsters = new ArrayList<Monster>();
        for (int i = 0; i < count; i++) {
            int x = 100 + random.nextInt(worldWidth - 200);
            int y = 100 + random.nextInt(worldHeight - 200);

            int baseLevel = 1 + (cycle * 2);
            int level = Math.max(1, baseLevel + random.nextInt(5) - 2);
            String type = MONSTER_TYPES[random.nextInt(MONSTER_TYPES.length)];

            monsters.add(new Monster(type, level, x, y));
        }
        return monsters;
    }

    public List<Resource> generateResources(int count) {
        List<Resource> resources = new ArrayList<Resource>();
        for (int i = 0; i < count; i++) {
            int x = 50 + random.nextInt(worldWidth - 100);
            int y = 50 + random.nextInt(worldHeight - 100);

            String type = RESOURCE_TYPES[random.nextInt(RESOURCE_TYPES.length)];
            int quality = 1 + random.nextInt(3);

            resources.add(new Resource(type, quality, x, y));
        }
        return resources;
    }
}
