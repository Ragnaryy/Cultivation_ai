package com.cultivation.game;

import android.content.*;
import android.util.*;
import java.util.*;

public class GameWorld {
    private static final String TAG = "GameWorld";
    private static final String SAVE_KEY = "cultivation_save";

    private Context context;
    public Player player;
    public List<Monster> monsters;
    public List<Resource> resources;
    private List<String> log;
    private Random random;
    private int day;
    private int cycle;
    private int worldWidth = 1000;
    private int worldHeight = 600;

    public GameWorld(Context context) {
        this.context = context;
        this.random = new Random();
        this.log = new ArrayList<String>();
        this.monsters = new ArrayList<Monster>();
        this.resources = new ArrayList<Resource>();

        loadGame();

        if (player == null) {
            initializeNewGame();
        }
    }

    private void initializeNewGame() {
        this.day = 1;
        this.cycle = 1;

        // Создаем игрока
        this.player = new Player("Небесный Избранник");
        player.x = worldWidth / 2;
        player.y = worldHeight / 2;

        generateWorld();

        addLog("🌍 Мир создан! День " + day);
        addLog("👤 Появился " + player.name);
    }

    private void generateWorld() {
        monsters.clear();
        resources.clear();

        // Создаем монстров
        String[] monsterTypes = {"Огненный Дракон", "Ледяной Феникс", "Громовой Тигр", "Земляная Черепаха"};
        for (int i = 0; i < 12; i++) {
            int x = 100 + random.nextInt(worldWidth - 200);
            int y = 100 + random.nextInt(worldHeight - 200);
            int level = 1 + random.nextInt(cycle * 2);
            monsters.add(new Monster(monsterTypes[random.nextInt(monsterTypes.length)], level, x, y));
        }

        // Создаем ресурсы
        String[] resourceTypes = {"Небесная Роса", "Корень Жизни", "Ядро Дракона", "Слеза Феникса"};
        for (int i = 0; i < 20; i++) {
            int x = 50 + random.nextInt(worldWidth - 100);
            int y = 50 + random.nextInt(worldHeight - 100);
            int quality = 1 + random.nextInt(3);
            resources.add(new Resource(resourceTypes[random.nextInt(resourceTypes.length)], quality, x, y));
        }

        addLog("🐉 Появились монстры: " + monsters.size());
        addLog("🌿 Появились ресурсы: " + resources.size());
    }

    public int getDay() {
        return day;
    }

    public int getCycle() {
        return cycle;
    }

    public int getAliveMonsters() {
        int count = 0;
        for (Monster monster : monsters) {
            if (monster.health > 0) count++;
        }
        return count;
    }

    public int getResourcesCount() {
        return resources.size();
    }

    public void update() {
        day++;

        // Новый цикл каждые 30 дней
        if (day % 30 == 0) {
            cycle++;
            addLog("🌅 Начинается новый цикл! (" + cycle + ")");
            generateWorld();
        }

        // Плавное восстановление
        player.health = Math.min(player.maxHealth, player.health + 2);
        player.qi = Math.min(player.maxQi, player.qi + 3);

        // Регенерация если нужно
        if (getAliveMonsters() < 6) {
            spawnNewMonsters();
        }
        if (resources.size() < 10) {
            spawnNewResources();
        }

        // Живое движение монстров
        moveMonsters();
    }

    private void spawnNewMonsters() {
        String[] monsterTypes = {"Огненный Дракон", "Ледяной Феникс", "Громовой Тигр", "Земляная Черепаха"};
        int toSpawn = 3 + random.nextInt(4);

        for (int i = 0; i < toSpawn; i++) {
            int x = 100 + random.nextInt(worldWidth - 200);
            int y = 100 + random.nextInt(worldHeight - 200);
            int level = 1 + random.nextInt(cycle * 2);
            monsters.add(new Monster(monsterTypes[random.nextInt(monsterTypes.length)], level, x, y));
        }
        addLog("🐉 В мир пришли новые духи");
    }

    private void spawnNewResources() {
        String[] resourceTypes = {"Небесная Роса", "Корень Жизни", "Ядро Дракона", "Слеза Феникса"};
        int toSpawn = 5 + random.nextInt(6);

        for (int i = 0; i < toSpawn; i++) {
            int x = 50 + random.nextInt(worldWidth - 100);
            int y = 50 + random.nextInt(worldHeight - 100);
            int quality = 1 + random.nextInt(3);
            resources.add(new Resource(resourceTypes[random.nextInt(resourceTypes.length)], quality, x, y));
        }
    }

    private void moveMonsters() {
        for (Monster monster : monsters) {
            if (monster.health > 0) {
                // Плавное движение к игроку если близко
                double distance = getDistance(player.x, player.y, monster.x, monster.y);
                if (distance < 250) {
                    // Движение к игроку
                    int dx = player.x - monster.x;
                    int dy = player.y - monster.y;
                    double length = Math.sqrt(dx * dx + dy * dy);
                    if (length > 0) {
                        monster.x += (int)(dx / length * 8);
                        monster.y += (int)(dy / length * 8);
                    }
                } else {
                    // Случайное блуждание
                    monster.x += random.nextInt(13) - 6;
                    monster.y += random.nextInt(13) - 6;
                }

                // Ограничение границ
                monster.x = Math.max(30, Math.min(worldWidth - 30, monster.x));
                monster.y = Math.max(30, Math.min(worldHeight - 30, monster.y));
            }
        }
    }

    public void aiAction() {
        // Умный ИИ с плавными переходами

        // 1. Критическое состояние - бегство и лечение
        if (player.health < 25) {
            escapeAndHeal();
            return;
        }

        // 2. Поиск лечения если здоровье среднее
        if (player.health < 60 && findHealingResource() != null) {
            Resource healing = findHealingResource();
            // Движение к ресурсу
            int dx = healing.x - player.x;
            int dy = healing.y - player.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                player.x += (int)(dx / length * 12);
                player.y += (int)(dy / length * 12);
            }
            if (getDistance(player.x, player.y, healing.x, healing.y) < 40) {
                playerGather();
            }
            return;
        }

        // 3. Атака слабых монстров
        Monster weakTarget = findWeakMonster();
        if (weakTarget != null && player.health > 50) {
            // Движение к монстру
            int dx = weakTarget.x - player.x;
            int dy = weakTarget.y - player.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                player.x += (int)(dx / length * 10);
                player.y += (int)(dy / length * 10);
            }
            if (getDistance(player.x, player.y, weakTarget.x, weakTarget.y) < 50) {
                playerAttack();
            }
            return;
        }

        // 4. Культивация при безопасности
        if (player.qi >= 25 && isSafe() && random.nextDouble() < 0.7) {
            playerCultivate();
            return;
        }

        // 5. Сбор ценных ресурсов
        Resource valuableResource = findValuableResource();
        if (valuableResource != null) {
            // Движение к ресурсу
            int dx = valuableResource.x - player.x;
            int dy = valuableResource.y - player.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length > 0) {
                player.x += (int)(dx / length * 8);
                player.y += (int)(dy / length * 8);
            }
            if (getDistance(player.x, player.y, valuableResource.x, valuableResource.y) < 40) {
                playerGather();
            }
            return;
        }

        // 6. Исследование неизвестных областей
        exploreNewAreas();
    }

    private void escapeAndHeal() {
        Monster nearest = findNearestMonster();
        if (nearest != null) {
            // Движение от монстра
            int dx = player.x - nearest.x;
            int dy = player.y - nearest.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                player.x += (int)(dx / distance * 25);
                player.y += (int)(dy / distance * 25);
            }
        }

        // Использование ресурсов для лечения
        if (findHealingResource() != null) {
            playerGather();
        } else {
            addLog("💨 Бегство от опасности");
        }
    }

    private void exploreNewAreas() {
        // Движение в случайном направлении
        player.x += random.nextInt(41) - 20;
        player.y += random.nextInt(41) - 20;

        // Ограничение границ мира
        player.x = Math.max(20, Math.min(worldWidth - 20, player.x));
        player.y = Math.max(20, Math.min(worldHeight - 20, player.y));

        addLog("🗺️ Исследование неизвестных земель");
        player.experience += 2;
    }

    public boolean playerCultivate() {
        if (player.qi >= 20) {
            player.qi -= 20;
            player.experience += 12;
            player.cultivationStage += 0.15;

            // Шанс прорыва растет со стадией
            double breakthroughChance = 0.08 + (player.cultivationStage * 0.02);

            if (random.nextDouble() < breakthroughChance) {
                // Большой прорыв
                player.level++;
                player.maxHealth += 30;
                player.maxQi += 20;
                player.power += 10;
                player.health = player.maxHealth;
                player.qi = player.maxQi;

                addLog("🌟 ПРОРЫВ В НОВЫЙ УРОВЕНЬ! (" + player.level + ")");
                return true;
            } else {
                addLog("🌀 Ци циркулирует по меридианам");
                return true;
            }
        }
        addLog("⚠️ Недостаточно ци для культивации");
        return false;
    }

    public boolean playerAttack() {
        Monster target = findNearestMonster();
        if (target != null && target.health > 0) {
            double distance = getDistance(player.x, player.y, target.x, target.y);

            if (distance < 60) {
                int damage = player.power + random.nextInt(16);
                target.health -= damage;

                addLog("⚡ Выпущена небесная кара! (-" + damage + " HP)");

                if (target.health <= 0) {
                    int exp = target.level * 20;
                    player.experience += exp;
                    addLog("🎯 " + target.type + " рассеян! +" + exp + " очков просветления");
                    checkLevelUp();
                    return true;
                }

                // Ответная атака
                if (target.health > 0 && random.nextDouble() < 0.5) {
                    int monsterDamage = target.power + random.nextInt(8);
                    player.health -= monsterDamage;
                    addLog("💥 " + target.type + " контратакует! (-" + monsterDamage + " HP)");

                    if (player.health <= 0) {
                        playerDie();
                    }
                }
                return true;
            }
        }
        addLog("📏 Нет досягаемых целей");
        return false;
    }

    public boolean playerGather() {
        Resource target = findNearestResource();
        if (target != null) {
            double distance = getDistance(player.x, player.y, target.x, target.y);

            if (distance < 50) {
                resources.remove(target);

                // Эффекты в зависимости от качества
                int bonus = target.quality * 10;

                if (target.type.equals("Небесная Роса")) {
                    player.health = Math.min(player.maxHealth, player.health + 20 + bonus);
                    addLog("💧 Поглощена небесная роса (+" + (20 + bonus) + " HP)");
                } else if (target.type.equals("Корень Жизни")) {
                    player.qi = Math.min(player.maxQi, player.qi + 25 + bonus);
                    addLog("🌱 Поглощен корень жизни (+" + (25 + bonus) + " ци)");
                } else if (target.type.equals("Ядро Дракона")) {
                    player.power += 2 + target.quality;
                    player.experience += 50 + bonus;
                    addLog("🐉 Получено ядро дракона (+" + (2 + target.quality) + " силы)");
                } else if (target.type.equals("Слеза Феникса")) {
                    player.maxHealth += 5;
                    player.maxQi += 5;
                    player.experience += 40 + bonus;
                    addLog("🔥 Получена слеза феникса (+5 к пределам)");
                }

                player.experience += 15;
                checkLevelUp();
                return true;
            }
        }
        addLog("📏 Нет доступных ресурсов");
        return false;
    }

    public boolean playerMeditate() {
        player.qi = Math.min(player.maxQi, player.qi + 30);
        player.health = Math.min(player.maxHealth, player.health + 15);
        player.experience += 8;

        addLog("💭 Глубокое познание небесного пути");
        checkLevelUp();
        return true;
    }

    private void checkLevelUp() {
        int requiredExp = player.level * 120;
        if (player.experience >= requiredExp) {
            player.level++;
            player.maxHealth += 25;
            player.power += 8;
            player.health = player.maxHealth;
            addLog("⬆️ Достигнут уровень " + player.level + " просветления!");
        }
    }

    private void playerDie() {
        player.deathCount++;

        // Прогрессивный штраф
        double penalty = Math.max(0.1, 0.25 - (player.deathCount * 0.02));
        player.experience = (int)(player.experience * (1 - penalty));

        // Воскрешение в случайном месте
        player.x = 200 + random.nextInt(worldWidth - 400);
        player.y = 200 + random.nextInt(worldHeight - 400);
        player.health = player.maxHealth / 3;
        player.qi = player.maxQi / 3;

        addLog("💀 Тело разрушено! Потеряно " + (int)(penalty * 100) + "% просветления");
        addLog("♻️ Воссоздание формы... Перерождений: " + player.deathCount);
    }

    // Вспомогательные методы поиска
    private Monster findNearestMonster() {
        Monster nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Monster monster : monsters) {
            if (monster.health > 0) {
                double distance = getDistance(player.x, player.y, monster.x, monster.y);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = monster;
                }
            }
        }
        return nearest;
    }

    private Resource findNearestResource() {
        Resource nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Resource resource : resources) {
            double distance = getDistance(player.x, player.y, resource.x, resource.y);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = resource;
            }
        }
        return nearest;
    }

    private Monster findWeakMonster() {
        for (Monster monster : monsters) {
            if (monster.health > 0 && monster.level <= player.level + 2) {
                double distance = getDistance(player.x, player.y, monster.x, monster.y);
                if (distance < 200) return monster;
            }
        }
        return null;
    }

    private Resource findHealingResource() {
        for (Resource resource : resources) {
            if (resource.type.equals("Небесная Роса") || resource.type.equals("Корень Жизни")) {
                double distance = getDistance(player.x, player.y, resource.x, resource.y);
                if (distance < 300) return resource;
            }
        }
        return null;
    }

    private Resource findValuableResource() {
        for (Resource resource : resources) {
            if (resource.quality >= 2) {
                double distance = getDistance(player.x, player.y, resource.x, resource.y);
                if (distance < 250) return resource;
            }
        }
        return findNearestResource();
    }

    private boolean isSafe() {
        for (Monster monster : monsters) {
            if (monster.health > 0) {
                double distance = getDistance(player.x, player.y, monster.x, monster.y);
                if (distance < 150) return false;
            }
        }
        return true;
    }

    private double getDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

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

            // Простое сохранение основных данных
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
            editor.putInt("player_x", player.x);
            editor.putInt("player_y", player.y);

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
                player.x = prefs.getInt("player_x", 500);
                player.y = prefs.getInt("player_y", 300);

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

    // Геттеры
    public Player getPlayer() { return player; }
    public int getWorldWidth() { return worldWidth; }
    public int getWorldHeight() { return worldHeight; }
}
