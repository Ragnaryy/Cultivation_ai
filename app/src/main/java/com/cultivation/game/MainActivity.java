package com.cultivation.game;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private GameWorld gameWorld;
    private GameView gameView;
    private TextView statsText, worldLog, worldInfo;
    private Button toggleAIbtn, cultivateBtn, attackBtn, gatherBtn, meditateBtn;

    private boolean autoMode = true;
    private Timer gameTimer;
    private long lastSaveTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main);

        initViews();
        loadGameWorld();
        startGameLoop();

        updateUI();
    }

    private void initViews() {
        statsText = (TextView) findViewById(R.id.statsText);
        worldLog = (TextView) findViewById(R.id.worldLog);
        worldInfo = (TextView) findViewById(R.id.worldInfo);

        toggleAIbtn = (Button) findViewById(R.id.toggleAIbtn);
        cultivateBtn = (Button) findViewById(R.id.cultivateBtn);
        attackBtn = (Button) findViewById(R.id.attackBtn);
        gatherBtn = (Button) findViewById(R.id.gatherBtn);
        meditateBtn = (Button) findViewById(R.id.meditateBtn);

        gameView = new GameView(this);
        FrameLayout gameContainer = (FrameLayout) findViewById(R.id.gameContainer);
        gameContainer.addView(gameView);

        setButtonListeners();
    }

    private void setButtonListeners() {
        toggleAIbtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    toggleAI();
                    v.startAnimation(android.view.animation.AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
                }
            });

        cultivateBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (gameWorld.playerCultivate()) {
                        showEffect("🌀", "Ци циркулирует!");
                    }
                    updateUI();
                }
            });

        attackBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (gameWorld.playerAttack()) {
                        showEffect("⚡", "Выпущена небесная кара!");
                    }
                    updateUI();
                }
            });

        gatherBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (gameWorld.playerGather()) {
                        showEffect("🌿", "Собрана энергия дао!");
                    }
                    updateUI();
                }
            });

        meditateBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (gameWorld.playerMeditate()) {
                        showEffect("💭", "Познание небесного пути!");
                    }
                    updateUI();
                }
            });
    }

    private void loadGameWorld() {
        gameWorld = new GameWorld(this);
        gameView.setGameWorld(gameWorld);

        if (gameWorld.isNewGame()) {
            gameWorld.addLog("🌌 Рождение нового Бессмертного");
            gameWorld.addLog("🌀 Начало циркуляции ци");
        } else {
            gameWorld.addLog("♻️ Возвращение в мир cultivation");
        }
    }

    private void startGameLoop() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    runOnUiThread(new Runnable() {
                            public void run() {
                                gameWorld.update();

                                if (autoMode) {
                                    gameWorld.aiAction();
                                }

                                if (System.currentTimeMillis() - lastSaveTime > 30000) {
                                    gameWorld.saveGame();
                                    lastSaveTime = System.currentTimeMillis();
                                }

                                updateUI();
                                gameView.invalidate();
                            }
                        });
                }
            }, 1000, 1500);
    }

    private void toggleAI() {
        autoMode = !autoMode;
        String status = autoMode ? "ВКЛ" : "ВЫКЛ";
        toggleAIbtn.setText("🌀 Авто-Культивация: " + status);
        gameWorld.addLog(autoMode ? "🤖 Включена авто-культивация" : "👐 Включено ручное управление");
    }

    private void showEffect(String symbol, String message) {
		gameWorld.addLog(symbol + " " + message);
	}

    private void updateUI() {
        if (gameWorld == null) return;

        Player player = gameWorld.getPlayer();

        // Форматируем время игры
        long totalSeconds = gameWorld.getTotalPlayTime();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        String playTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        String stats = "👤 " + player.name + 
            "\n⚡ Уровень: " + player.level + 
            "\n🌟 Стадия: " + String.format("%.1f", player.cultivationStage) +
            "\n💫 Титул: " + player.getRankTitle() +
            "\n\n❤️ Здоровье: " + player.health + "/" + player.maxHealth +
            "\n🌀 Ци: " + player.qi + "/" + player.maxQi +
            "\n🎯 Сила: " + player.power +
            "\n\n📚 Опыт: " + player.experience + 
            "\n📊 Множитель опыта: " + String.format("%.2fx", player.getExperienceMultiplier()) +
            "\n\n⏱️ Время игры: " + playTime +
            "\n♻️ Перерождений: " + player.deathCount +
            "\n\n🏆 Общий ранг: " + player.getTotalRank() +
            "\n💪 Физ. ранг: " + player.physicalRank +
            "\n🧠 Дух. ранг: " + player.spiritualRank +
            "\n⚔️ Боев. ранг: " + player.combatRank;

        statsText.setText(stats);
        worldLog.setText(gameWorld.getLog());

        String worldStats = "Эра: " + gameWorld.getDay() + 
            "\nЦикл: " + gameWorld.getCycle() +
            "\n\nМонстров: " + gameWorld.getAliveMonsters() +
            "\nРесурсов: " + gameWorld.getResourcesCount() +
            "\n\n📊 Статистика:" +
            "\nСобрано ресурсов: " + gameWorld.getResourcesCollected() +
            "\nПобеждено монстров: " + gameWorld.getMonstersDefeated() +
            "\nСеансов культивации: " + gameWorld.getCultivationSessions() +
            "\nСеансов медитации: " + gameWorld.getMeditationSessions() +
            "\nПрорывов: " + gameWorld.getBreakthroughs() +
            "\n\nПозиция: " + player.position.x + "," + player.position.y;
        worldInfo.setText(worldStats);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
        if (gameWorld != null) {
            gameWorld.saveGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameTimer == null) {
            startGameLoop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        if (gameWorld != null) {
            gameWorld.saveGame();
        }
    }
}
