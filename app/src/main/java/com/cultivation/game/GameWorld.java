package com.cultivation.game;

import android.content.*;
import android.util.*;
import java.util.*;

public class GameWorld {
    private static final String TAG = "GameWorld";

    private Context context;
    private Player player;
    private List<Monster> monsters;
    private List<Resource> resources;
    private List<String> log;
    private Random random;
    private WorldGenerator worldGenerator;
    private AIController aiController;

    private int day;
    private int cycle;
    private int worldWidth = 1000;
    private int worldHeight = 600;

    // Новые поля для отслеживания времени
    private long gameStartTime;
    private long lastUpdateTime;

    public GameWorld(Context context) {
        this.context = context;
        this.random = new Random();
        this.log = new ArrayList<String>();
        this.monsters = new ArrayList<Monster>();
        this.resources = new ArrayList<Resource>();
        this.worldGenerator = new WorldGenerator(worldWidth, worldHeight);

        this.gameStartTime = System.currentTimeMillis();
        this.lastUpdateTime = gameStartTime;

        loadGame();

        if (player == null) {
            initializeNewGame();
        }

        this.aiController = new AIController(this);
    }

    private void initializeNewGame() {
        this.day = 1;
        this.cycle = 1;

        this.player = new Player("Небесный Избранник");
        generateWorld();

        addLog("🌍 Мир создан! День " + day);
        addLog("👤 Появился " + player.name);
    }

    private void generateWorld() {
        // Увеличиваем сложность в зависимости от прогресса игрока
        int monsterCount = 12 + (cycle * 2) + (player.getTotalRank() / 50);
        int resourceCount = 20 + (player.getTotalRank() / 30);

        monsters = worldGenerator.generateMonsters(Math.min(monsterCount, 50), cycle);
        resources = worldGenerator.generateResources(Math.min(resourceCount, 100));

        addLog("🐉 Появились монстры: " + monsters.size());
        addLog("🌿 Появились ресурсы: " + resources.size());
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = (currentTime - lastUpdateTime) / 1000; // в секундах
        lastUpdateTime = currentTime;

        // Обновляем общее время игры игрока
        player.addPlayTime(deltaTime);

        day++;

        // Циклы теперь зависят от прогресса игрока
        int cyclesPerEra = Math.max(20, 30 - (player.getTotalRank() / 100));
        if (day % cyclesPerEra == 0) {
            cycle++;
            addLog("🌅 Начинается новый цикл! (" + cycle + ")");
            generateWorld();
        }

        player.regenerate();

        // Динамическое появление монстров и ресурсов в зависимости от прогресса
        int minMonsters = 6 + (player.getTotalRank() / 40);
        if (getAliveMonsters() < minMonsters) {
            spawnNewMonsters();
        }

        int minResources = 10 + (player.getTotalRank() / 30);
        if (resources.size() < minResources) {
            spawnNewResources();
        }

        moveMonsters();
    }

    private void spawnNewMonsters() {
        int count = 3 + random.nextInt(4) + (player.getTotalRank() / 60);
        List<Monster> newMonsters = worldGenerator.generateMonsters(count, cycle);
        monsters.addAll(newMonsters);
        addLog("🐉 В мир пришли новые духи");
    }

    private void spawnNewResources() {
        int count = 5 + random.nextInt(6) + (player.getTotalRank() / 40);
        List<Resource> newResources = worldGenerator.generateResources(count);
        resources.addAll(newResources);
    }

    private void moveMonsters() {
        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                double distance = player.position.distanceTo(monster.position);
                // Скорость монстров увеличивается с прогрессом игрока
                int speed = 8 + (player.getTotalRank() / 100);
                if (distance < 250) {
                    monster.position.moveTowards(player.position, speed);
                } else {
                    monster.position.x += random.nextInt(13) - 6;
                    monster.position.y += random.nextInt(13) - 6;
                }
                monster.position.clamp(30, 30, worldWidth - 30, worldHeight - 30);
            }
        }
    }

    public void aiAction() {
        aiController.performAction();
    }

    public boolean playerCultivate() {
        if (player.canCultivate()) {
            player.qi -= 20;

            // Опыт с учетом множителя
            double expMultiplier = player.getExperienceMultiplier();
            int baseExp = 12;
            int actualExp = (int)(baseExp * expMultiplier);

            player.experience += actualExp;
            player.cultivationStage += 0.15;
            player.incrementCultivationSessions();

            // Шанс прорыва увеличивается с рангом
            double breakthroughChance = 0.08 + (player.cultivationStage * 0.02) + (player.getTotalRank() * 0.0001);

            if (random.nextDouble() < breakthroughChance) {
                player.level++;
                player.maxHealth += 30 + (player.getTotalRank() / 20);
                player.maxQi += 20 + (player.getTotalRank() / 25);
                player.power += 10 + (player.getTotalRank() / 30);
                player.health = player.maxHealth;
                player.qi = player.maxQi;
                player.incrementBreakthroughs();

                addLog("🌟 ПРОРЫВ В НОВЫЙ УРОВЕНЬ! (" + player.level + ")");
                return true;
            } else {
                addLog("🌀 Ци циркулирует по меридианам (+" + actualExp + " опыта)");
                return true;
            }
        }
        addLog("⚠️ Недостаточно ци для культивации");
        return false;
    }

    public boolean playerAttack() {
        Monster target = findNearestMonster();
        if (target != null && target.isAlive()) {
            double distance = player.position.distanceTo(target.position);

            if (distance < 60) {
                // Урон увеличивается с рангом
                int baseDamage = player.power + random.nextInt(16);
                int rankBonus = player.getTotalRank() / 20;
                int damage = baseDamage + rankBonus;

                target.takeDamage(damage);

                addLog("⚡ Атакован [" + target.getRarityName() + "] " + target.type);
                addLog("   🎯 " + target.getRankTitle() + " Ур." + target.level);

                if (!target.isAlive()) {
                    // Опыт за монстра с учетом множителя и редкости
                    double expMultiplier = player.getExperienceMultiplier();
                    int actualExp = target.getExpReward();

                    player.experience += actualExp;
                    player.incrementMonstersDefeated();

                    addLog("🎯 Победа! +" + actualExp + " очков просветления");

                    // Дроп ресурсов с монстров
                    if (target.shouldDropResource()) {
                        Resource drop = createMonsterDrop(target);
                        resources.add(drop);
                        addLog("💎 Выпало: " + drop.type + " " + drop.getQualityStars());
                    }

                    checkLevelUp();
                    return true;
                }

                if (target.isAlive() && random.nextDouble() < 0.5) {
                    // Урон монстра тоже увеличивается с прогрессом
                    int monsterDamage = target.power + random.nextInt(8) + (cycle * 2);
                    player.takeDamage(monsterDamage);
                    addLog("💥 " + target.type + " контратакует! (-" + monsterDamage + " HP)");

                    if (!player.isAlive()) {
                        playerDie();
                    }
                }
                return true;
            }
        }
        addLog("📏 Нет досягаемых целей");
        return false;
    }

    private Resource createMonsterDrop(Monster monster) {
        int quality = Math.min(4, monster.rarity + 1);
        int x = monster.position.x + random.nextInt(50) - 25;
        int y = monster.position.y + random.nextInt(50) - 25;

        String[] dropTypes = {"Ядро Дракона", "Слеза Феникса", "Сердце Зверя", "Когть Титана", "Чешуя Дракона"};
        String type = dropTypes[random.nextInt(dropTypes.length)];

        return new Resource(type, quality, x, y, "monster");
    }

    public boolean playerGather() {
        Resource target = findNearestResource();
        if (target != null) {
            double distance = player.position.distanceTo(target.position);

            if (distance < 50) {
                resources.remove(target);
                applyResourceEffect(target);

                // Опыт с учетом множителя
                double expMultiplier = player.getExperienceMultiplier();
                int baseExp = 15;
                int actualExp = (int)(baseExp * expMultiplier);

                player.experience += actualExp;
                player.incrementResourcesCollected();
                checkLevelUp();
                return true;
            }
        }
        addLog("📏 Нет доступных ресурсов");
        return false;
    }

    private void applyResourceEffect(Resource resource) {
        // Эффект ресурсов усиливается с рангом игрока
        int rankBonus = player.getTotalRank() / 25;
        int baseBonus = resource.getBonusValue();
        int totalBonus = baseBonus + rankBonus;

        if ("Небесная Роса".equals(resource.type)) {
            int healAmount = 20 + totalBonus;
            player.heal(healAmount);
            addLog("💧 Поглощена небесная роса (+" + healAmount + " HP)");
        } else if ("Корень Жизни".equals(resource.type)) {
            int qiAmount = 25 + totalBonus;
            player.qi = Math.min(player.maxQi, player.qi + qiAmount);
            addLog("🌱 Поглощен корень жизни (+" + qiAmount + " ци)");
        } else if ("Ядро Дракона".equals(resource.type) || "Сердце Зверя".equals(resource.type)) {
            int powerBonus = 2 + resource.quality + (player.getTotalRank() / 50);
            player.power += powerBonus;
            player.experience += 50 + totalBonus;
            addLog("🐉 Получено " + resource.type + " (+" + powerBonus + " силы)");
        } else if ("Слеза Феникса".equals(resource.type) || "Когть Титана".equals(resource.type)) {
            int statBonus = 5 + (player.getTotalRank() / 40);
            player.maxHealth += statBonus;
            player.maxQi += statBonus;
            player.experience += 40 + totalBonus;
            addLog("🔥 Получен " + resource.type + " (+" + statBonus + " к пределам)");
        } else {
            // Для остальных ресурсов
            player.experience += 30 + totalBonus;
            addLog("✨ Получен " + resource.type + " (+" + (30 + totalBonus) + " опыта)");
        }
    }

    public boolean playerMeditate() {
        // Эффективность медитации увеличивается с рангом
        int rankBonus = player.getTotalRank() / 30;
        player.qi = Math.min(player.maxQi, player.qi + 30 + rankBonus);
        player.heal(15 + rankBonus);

        // Опыт с учетом множителя
        double expMultiplier = player.getExperienceMultiplier();
        int baseExp = 8;
        int actualExp = (int)(baseExp * expMultiplier);

        player.experience += actualExp;
        player.incrementMeditationSessions();

        addLog("💭 Глубокое познание небесного пути (+" + actualExp + " опыта)");
        checkLevelUp();
        return true;
    }

    private void checkLevelUp() {
        // Требуемый опыт увеличивается, но компенсируется множителем
        int baseRequiredExp = player.level * 120;
        double reduction = Math.min(0.5, player.getTotalRank() * 0.001);
        int requiredExp = (int)(baseRequiredExp * (1.0 - reduction));

        if (player.experience >= requiredExp) {
            player.level++;
            player.maxHealth += 25 + (player.getTotalRank() / 25);
            player.power += 8 + (player.getTotalRank() / 30);
            player.health = player.maxHealth;
            addLog("⬆️ Достигнут уровень " + player.level + " просветления!");
        }
    }

    private void playerDie() {
        player.deathCount++;
        // Штраф уменьшается с рангом
        double rankReduction = Math.min(0.15, player.getTotalRank() * 0.0003);
        double basePenalty = 0.25;
        double actualPenalty = Math.max(0.05, basePenalty - rankReduction);

        player.experience = (int)(player.experience * (1 - actualPenalty));

        player.position.x = 200 + random.nextInt(worldWidth - 400);
        player.position.y = 200 + random.nextInt(worldHeight - 400);
        player.health = player.maxHealth / 3;
        player.qi = player.maxQi / 3;

        addLog("💀 Тело разрушено! Потеряно " + (int)(actualPenalty * 100) + "% просветления");
        addLog("♻️ Воссоздание формы... Перерождений: " + player.deathCount);
    }

    // Методы поиска для AI
    public Monster findNearestMonster() {
        Monster nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                double distance = player.position.distanceTo(monster.position);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = monster;
                }
            }
        }
        return nearest;
    }

    public Resource findNearestResource() {
        Resource nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Resource resource : resources) {
            double distance = player.position.distanceTo(resource.position);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = resource;
            }
        }
        return nearest;
    }

    public Monster findWeakMonster() {
        for (Monster monster : monsters) {
            if (monster.isAlive() && monster.level <= player.level + 2 + (player.getTotalRank() / 100)) {
                double distance = player.position.distanceTo(monster.position);
                if (distance < 200) return monster;
            }
        }
        return null;
    }

    public Resource findHealingResource() {
        for (Resource resource : resources) {
            if (resource.isHealingType()) {
                double distance = player.position.distanceTo(resource.position);
                if (distance < 300) return resource;
            }
        }
        return null;
    }

    public Resource findValuableResource() {
        // Порог качества увеличивается с рангом
        int qualityThreshold = 2 + (player.getTotalRank() / 200);
        for (Resource resource : resources) {
            if (resource.quality >= qualityThreshold) {
                double distance = player.position.distanceTo(resource.position);
                if (distance < 250) return resource;
            }
        }
        return findNearestResource();
    }

    public boolean isSafe() {
        // Безопасная дистанция увеличивается с рангом
        int safeDistance = 150 + (player.getTotalRank() / 10);
        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                double distance = player.position.distanceTo(monster.position);
                if (distance < safeDistance) return false;
            }
        }
        return true;
    }

    // Геттеры
    public int getDay() { return day; }
    public int getCycle() { return cycle; }
    public int getAliveMonsters() { 
        int count = 0;
        for (Monster monster : monsters) {
            if (monster.isAlive()) count++;
        }
        return count;
    }
    public int getResourcesCount() { return resources.size(); }
    public Player getPlayer() { return player; }
    public int getWorldWidth() { return worldWidth; }
    public int getWorldHeight() { return worldHeight; }
    public List<Monster> getMonsters() { return monsters; }
    public List<Resource> getResources() { return resources; }

    // Новые геттеры для статистики
    public long getTotalPlayTime() { return player.totalPlayTime; }
    public int getResourcesCollected() { return player.resourcesCollected; }
    public int getMonstersDefeated() { return player.monstersDefeated; }
    public int getCultivationSessions() { return player.cultivationSessions; }
    public int getMeditationSessions() { return player.meditationSessions; }
    public int getBreakthroughs() { return player.breakthroughs; }

    public void addLog(String message) {
        log.add(0, message);
        if (log.size() > 10) {
            log.remove(log.size() - 1);
        }
    }

    public String getLog() {
        StringBuilder sb = new StringBuilder();
        for (String entry : log) {
            sb.append(entry).append("\n\n");
        }
        return sb.toString();
    }

    // Сохранение и загрузка
    public void saveGame() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("CultivationData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putInt("day", day);
            editor.putInt("cycle", cycle);
            editor.putInt("player_level", player.level);
            editor.putInt("player_health", player.health);
            editor.putInt("player_maxHealth", player.maxHealth);
            editor.putInt("player_qi", player.qi);
            editor.putInt("player_maxQi", player.maxQi);
            editor.putInt("player_power", player.power);
            editor.putInt("player_experience", player.experience);
            editor.putFloat("player_cultivationStage", (float)player.cultivationStage);
            editor.putInt("player_deathCount", player.deathCount);
            editor.putInt("player_x", player.position.x);
            editor.putInt("player_y", player.position.y);

            // Сохранение новых полей
            editor.putLong("player_totalPlayTime", player.totalPlayTime);
            editor.putInt("player_resourcesCollected", player.resourcesCollected);
            editor.putInt("player_monstersDefeated", player.monstersDefeated);
            editor.putInt("player_cultivationSessions", player.cultivationSessions);
            editor.putInt("player_meditationSessions", player.meditationSessions);
            editor.putInt("player_breakthroughs", player.breakthroughs);
            editor.putInt("player_physicalRank", player.physicalRank);
            editor.putInt("player_spiritualRank", player.spiritualRank);
            editor.putInt("player_combatRank", player.combatRank);

            editor.apply();
            Log.d(TAG, "Игра сохранена");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка сохранения: " + e.getMessage());
        }
    }

    private void loadGame() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("CultivationData", Context.MODE_PRIVATE);

            if (prefs.contains("player_level")) {
                day = prefs.getInt("day", 1);
                cycle = prefs.getInt("cycle", 1);

                player = new Player("Небесный Избранник");
                player.level = prefs.getInt("player_level", 1);
                player.health = prefs.getInt("player_health", 100);
                player.maxHealth = prefs.getInt("player_maxHealth", 100);
                player.qi = prefs.getInt("player_qi", 50);
                player.maxQi = prefs.getInt("player_maxQi", 50);
                player.power = prefs.getInt("player_power", 10);
                player.experience = prefs.getInt("player_experience", 0);
                player.cultivationStage = prefs.getFloat("player_cultivationStage", 1.0f);
                player.deathCount = prefs.getInt("player_deathCount", 0);
                player.position.x = prefs.getInt("player_x", 500);
                player.position.y = prefs.getInt("player_y", 300);

                // Загрузка новых полей
                player.totalPlayTime = prefs.getLong("player_totalPlayTime", 0);
                player.resourcesCollected = prefs.getInt("player_resourcesCollected", 0);
                player.monstersDefeated = prefs.getInt("player_monstersDefeated", 0);
                player.cultivationSessions = prefs.getInt("player_cultivationSessions", 0);
                player.meditationSessions = prefs.getInt("player_meditationSessions", 0);
                player.breakthroughs = prefs.getInt("player_breakthroughs", 0);
                player.physicalRank = prefs.getInt("player_physicalRank", 10);
                player.spiritualRank = prefs.getInt("player_spiritualRank", 10);
                player.combatRank = prefs.getInt("player_combatRank", 10);

                generateWorld();
                addLog("♻️ Загрузка предыдущего просветления...");
                Log.d(TAG, "Игра загружена");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки: " + e.getMessage());
        }
    }

    public boolean isNewGame() {
        SharedPreferences prefs = context.getSharedPreferences("CultivationData", Context.MODE_PRIVATE);
        return !prefs.contains("player_level");
    }
}
