package com.cultivation.game;

public class Player extends LivingEntity {
    public String name;
    public int qi;
    public int maxQi;
    public int experience;
    public double cultivationStage;
    public int deathCount;

    public int physicalRank;
    public int spiritualRank;
    public int combatRank;

    // Новые поля для долгосрочного прогресса
    public long totalPlayTime; // в секундах
    public int resourcesCollected;
    public int monstersDefeated;
    public int cultivationSessions;
    public int meditationSessions;
    public int breakthroughs;

    public Player(String name) {
        super(500, 300);
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

        this.physicalRank = 10;
        this.spiritualRank = 10;
        this.combatRank = 10;

        // Инициализация новых полей
        this.totalPlayTime = 0;
        this.resourcesCollected = 0;
        this.monstersDefeated = 0;
        this.cultivationSessions = 0;
        this.meditationSessions = 0;
        this.breakthroughs = 0;
    }

    public int getTotalRank() {
        // Учитываем все аспекты прогресса
        int baseRank = (physicalRank + spiritualRank + combatRank) / 3;
        int timeBonus = (int)(totalPlayTime / 3600); // 1 очко за каждый час игры
        int resourceBonus = resourcesCollected / 50; // 1 очко за каждые 50 ресурсов
        int monsterBonus = monstersDefeated / 20; // 1 очко за каждые 20 монстров
        int cultivationBonus = cultivationSessions / 10; // 1 очко за каждые 10 сессий

        return baseRank + timeBonus + resourceBonus + monsterBonus + cultivationBonus;
    }

    public String getRankTitle() {
        int total = getTotalRank();
        if (total < 50) return "Новичок";
        if (total < 100) return "Ученик";
        if (total < 200) return "Воин";
        if (total < 350) return "Мастер";
        if (total < 500) return "Просветленный";
        if (total < 750) return "Бессмертный";
        if (total < 1000) return "Небесный";
        if (total < 1500) return "Владыка";
        if (total < 2000) return "Небожитель";
        if (total < 3000) return "Создатель";
        return "Властелин Вселенной";
    }

    public void regenerate() {
        health = Math.min(maxHealth, health + 2);
        qi = Math.min(maxQi, qi + 3);
    }

    public boolean canCultivate() {
        return qi >= 20;
    }

    // Новые методы для отслеживания прогресса
    public void addPlayTime(long seconds) {
        totalPlayTime += seconds;
    }

    public void incrementResourcesCollected() {
        resourcesCollected++;
        // Автоматическое улучшение характеристик при сборе ресурсов
        if (resourcesCollected % 25 == 0) {
            spiritualRank++;
        }
    }

    public void incrementMonstersDefeated() {
        monstersDefeated++;
        // Автоматическое улучшение характеристик при победе над монстрами
        if (monstersDefeated % 15 == 0) {
            combatRank++;
            physicalRank++;
        }
    }

    public void incrementCultivationSessions() {
        cultivationSessions++;
        // Бонус за регулярную культивацию
        if (cultivationSessions % 5 == 0) {
            spiritualRank += 2;
        }
    }

    public void incrementMeditationSessions() {
        meditationSessions++;
        // Бонус за медитацию
        if (meditationSessions % 3 == 0) {
            spiritualRank++;
            maxQi += 2;
        }
    }

    public void incrementBreakthroughs() {
        breakthroughs++;
        // Значительный бонус за прорыв
        physicalRank += 3;
        spiritualRank += 3;
        combatRank += 3;
    }

    // Метод для расчета множителя опыта на основе ранга и времени
    public double getExperienceMultiplier() {
        double multiplier = 1.0;

        // Бонус за время игры (до +50%)
        multiplier += Math.min(0.5, totalPlayTime / 72000.0); // 20 часов = +50%

        // Бонус за ранг (до +100%)
        multiplier += Math.min(1.0, getTotalRank() / 500.0);

        // Бонус за количество прорывов (до +50%)
        multiplier += Math.min(0.5, breakthroughs / 20.0);

        return multiplier;
    }
}
