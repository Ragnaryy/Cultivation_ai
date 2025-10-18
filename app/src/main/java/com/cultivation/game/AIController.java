package com.cultivation.game;

import java.util.*;

public class AIController {
    private GameWorld gameWorld;
    private Random random;

    public AIController(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.random = new Random();
    }

    public void performAction() {
        Player player = gameWorld.getPlayer();

        // 1. Критическое состояние - бегство и лечение
        if (player.health < 25) {
            escapeAndHeal();
            return;
        }

        // 2. Поиск лечения если здоровье среднее
        if (player.health < 60) {
            Resource healing = gameWorld.findHealingResource();
            if (healing != null) {
                moveAndGather(healing, 12);
                return;
            }
        }

        // 3. Атака слабых монстров
        Monster weakTarget = gameWorld.findWeakMonster();
        if (weakTarget != null && player.health > 50) {
            moveAndAttack(weakTarget, 10);
            return;
        }

        // 4. Культивация при безопасности
        if (player.qi >= 25 && gameWorld.isSafe() && random.nextDouble() < 0.7) {
            gameWorld.playerCultivate();
            return;
        }

        // 5. Сбор ценных ресурсов
        Resource valuableResource = gameWorld.findValuableResource();
        if (valuableResource != null) {
            moveAndGather(valuableResource, 8);
            return;
        }

        // 6. Исследование неизвестных областей
        exploreNewAreas();
    }

    private void escapeAndHeal() {
        Player player = gameWorld.getPlayer();
        Monster nearest = gameWorld.findNearestMonster();

        if (nearest != null) {
            player.position.moveTowards(nearest.position, -25); // Движение от монстра
        }

        if (gameWorld.findHealingResource() != null) {
            gameWorld.playerGather();
        } else {
            gameWorld.addLog("💨 Бегство от опасности");
        }
    }

    private void moveAndGather(Resource resource, int speed) {
        Player player = gameWorld.getPlayer();
        player.position.moveTowards(resource.position, speed);
        player.position.clamp(20, 20, gameWorld.getWorldWidth() - 20, gameWorld.getWorldHeight() - 20);

        if (player.position.distanceTo(resource.position) < 40) {
            gameWorld.playerGather();
        }
    }

    private void moveAndAttack(Monster monster, int speed) {
        Player player = gameWorld.getPlayer();
        player.position.moveTowards(monster.position, speed);
        player.position.clamp(20, 20, gameWorld.getWorldWidth() - 20, gameWorld.getWorldHeight() - 20);

        if (player.position.distanceTo(monster.position) < 50) {
            gameWorld.playerAttack();
        }
    }

    private void exploreNewAreas() {
        Player player = gameWorld.getPlayer();
        player.position.x += random.nextInt(41) - 20;
        player.position.y += random.nextInt(41) - 20;
        player.position.clamp(20, 20, gameWorld.getWorldWidth() - 20, gameWorld.getWorldHeight() - 20);

        gameWorld.addLog("🗺️ Исследование неизвестных земель");
        player.experience += 2;
    }
}
