package com.cultivation.game;

import android.content.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class GameView extends View {
    private GameWorld gameWorld;
    private Paint paint;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private List<Particle> particles;
    private long lastUpdateTime;

    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        particles = new ArrayList<Particle>();
        lastUpdateTime = System.currentTimeMillis();
    }

    public void setGameWorld(GameWorld world) {
        this.gameWorld = world;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (gameWorld != null) {
            scaleX = (float)w / gameWorld.getWorldWidth();
            scaleY = (float)h / gameWorld.getWorldHeight();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        // Обновляем частицы
        updateParticles(deltaTime);

        // Рисуем мир
        drawWorld(canvas);
        drawTerrain(canvas);
        drawResources(canvas);
        drawMonsters(canvas);
        drawPlayer(canvas);
        drawParticles(canvas);
        drawUI(canvas);

        // Плавная перерисовка
        postInvalidateDelayed(50);
    }

    private void drawWorld(Canvas canvas) {
        // Градиентный фон
        Paint bgPaint = new Paint();
        int startColor = Color.argb(255, 10, 10, 30);
        int endColor = Color.argb(255, 20, 20, 40);
        bgPaint.setShader(new LinearGradient(0, 0, getWidth(), getHeight(), 
											 startColor, endColor, Shader.TileMode.MIRROR));
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        // Звезды
        paint.setColor(Color.argb(150, 255, 255, 255));
        Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            int x = rand.nextInt(getWidth());
            int y = rand.nextInt(getHeight());
            float size = rand.nextFloat() * 3;
            canvas.drawCircle(x, y, size, paint);
        }
    }

    private void drawTerrain(Canvas canvas) {
        // Реки
        paint.setColor(Color.argb(120, 0, 100, 200));
        canvas.drawCircle(getWidth() * 0.3f, getHeight() * 0.4f, 80, paint);
        canvas.drawCircle(getWidth() * 0.7f, getHeight() * 0.6f, 60, paint);

        // Горы
        paint.setColor(Color.argb(200, 120, 80, 50));

        // Гора 1
        Path mountain1 = new Path();
        mountain1.moveTo(getWidth() * 0.2f, getHeight() * 0.8f);
        mountain1.lineTo(getWidth() * 0.3f, getHeight() * 0.5f);
        mountain1.lineTo(getWidth() * 0.4f, getHeight() * 0.8f);
        mountain1.close();
        canvas.drawPath(mountain1, paint);

        // Гора 2
        Path mountain2 = new Path();
        mountain2.moveTo(getWidth() * 0.6f, getHeight() * 0.8f);
        mountain2.lineTo(getWidth() * 0.7f, getHeight() * 0.4f);
        mountain2.lineTo(getWidth() * 0.8f, getHeight() * 0.8f);
        mountain2.close();
        canvas.drawPath(mountain2, paint);
    }

    private void drawPlayer(Canvas canvas) {
        if (gameWorld == null || gameWorld.getPlayer() == null) return;

        Player player = gameWorld.getPlayer();
        int screenX = (int)(player.x * scaleX);
        int screenY = (int)(player.y * scaleY);

        // Аура игрока
        paint.setColor(Color.argb(80, 100, 200, 255));
        canvas.drawCircle(screenX, screenY, 25, paint);

        // Основное тело
        paint.setColor(Color.argb(255, 100, 200, 255));
        canvas.drawCircle(screenX, screenY, 15, paint);

        // Вращающиеся сферы ци
        float time = System.currentTimeMillis() * 0.001f;
        float orbit1 = 20;
        float orbit2 = 30;

        paint.setColor(Color.argb(200, 200, 255, 255));
        canvas.drawCircle(screenX + (float)Math.cos(time) * orbit1, 
						  screenY + (float)Math.sin(time) * orbit1, 5, paint);

        paint.setColor(Color.argb(150, 100, 255, 200));
        canvas.drawCircle(screenX + (float)Math.cos(time * 1.5f) * orbit2, 
						  screenY + (float)Math.sin(time * 1.5f) * orbit2, 4, paint);

        // Имя и уровень
        paint.setColor(Color.WHITE);
        paint.setTextSize(14);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(player.name, screenX, screenY - 35, paint);
        canvas.drawText("Lv." + player.level, screenX, screenY - 20, paint);
    }

    private void drawMonsters(Canvas canvas) {
        if (gameWorld == null || gameWorld.monsters == null) return;

        for (int i = 0; i < gameWorld.monsters.size(); i++) {
            Monster monster = gameWorld.monsters.get(i);
            if (monster.health > 0) {
                int screenX = (int)(monster.x * scaleX);
                int screenY = (int)(monster.y * scaleY);

                // Аура монстра
                paint.setColor(Color.argb(60, 255, 100, 100));
                canvas.drawCircle(screenX, screenY, 20, paint);

                // Тело монстра
                paint.setColor(Color.argb(255, 255, 100, 100));
                canvas.drawCircle(screenX, screenY, 12, paint);

                // Эффекты в зависимости от типа
                if (monster.type.equals("Огненный Дракон")) {
                    addFireEffect(screenX, screenY);
                } else if (monster.type.equals("Ледяной Феникс")) {
                    addIceEffect(screenX, screenY);
                } else if (monster.type.equals("Громовой Тигр")) {
                    addLightningEffect(screenX, screenY);
                }

                // Информация
                paint.setColor(Color.WHITE);
                paint.setTextSize(10);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(monster.type, screenX, screenY - 25, paint);
                canvas.drawText("Lv." + monster.level, screenX, screenY - 15, paint);

                // Полоска здоровья
                drawHealthBar(canvas, screenX - 20, screenY + 15, 40, 
							  monster.health, monster.maxHealth);
            }
        }
    }

    private void drawResources(Canvas canvas) {
        if (gameWorld == null || gameWorld.resources == null) return;

        for (int i = 0; i < gameWorld.resources.size(); i++) {
            Resource resource = gameWorld.resources.get(i);
            int screenX = (int)(resource.x * scaleX);
            int screenY = (int)(resource.y * scaleY);

            // Свечение ресурса
            paint.setColor(Color.argb(100, 100, 255, 150));
            canvas.drawCircle(screenX, screenY, 15, paint);

            // Основной ресурс
            paint.setColor(Color.argb(255, 100, 255, 150));

            // Разная форма в зависимости от типа
            if (resource.type.equals("Небесная Роса")) {
                canvas.drawCircle(screenX, screenY, 8, paint);
            } else if (resource.type.equals("Корень Жизни")) {
                canvas.drawRect(screenX - 6, screenY - 8, screenX + 6, screenY + 8, paint);
            } else if (resource.type.equals("Ядро Дракона")) {
                Path star = createStar(screenX, screenY, 10, 5);
                canvas.drawPath(star, paint);
            } else if (resource.type.equals("Слеза Феникса")) {
                canvas.drawCircle(screenX, screenY, 6, paint);
                paint.setColor(Color.YELLOW);
                canvas.drawCircle(screenX, screenY, 3, paint);
                paint.setColor(Color.argb(255, 100, 255, 150));
            }

            // Качество (звезды)
            paint.setColor(Color.YELLOW);
            for (int j = 0; j < resource.quality; j++) {
                canvas.drawCircle(screenX - 12 + j * 8, screenY - 15, 2, paint);
            }
        }
    }

    private void drawHealthBar(Canvas canvas, int x, int y, int width, int current, int max) {
        // Фон
        paint.setColor(Color.argb(200, 100, 100, 100));
        canvas.drawRect(x, y, x + width, y + 6, paint);

        // Здоровье
        if (current > 0) {
            int fillWidth = (int)(width * ((float)current / max));
            int green = (int)(255 * ((float)current / max));
            int red = 255 - green;
            paint.setColor(Color.argb(200, red, green, 0));
            canvas.drawRect(x, y, x + fillWidth, y + 6, paint);
        }
    }

    private void drawUI(Canvas canvas) {
        paint.setColor(Color.argb(200, 255, 255, 255));
        paint.setTextSize(12);
        paint.setTextAlign(Paint.Align.LEFT);

        if (gameWorld != null) {
            canvas.drawText("Цикл: " + gameWorld.getCycle(), 10, 20, paint);
            canvas.drawText("День: " + gameWorld.getDay(), 10, 35, paint);
            canvas.drawText("Монстров: " + gameWorld.getAliveMonsters(), 10, 50, paint);
            canvas.drawText("Ресурсов: " + gameWorld.getResourcesCount(), 10, 65, paint);

            Player player = gameWorld.getPlayer();
            if (player != null) {
                canvas.drawText("Позиция: " + player.x + "," + player.y, 10, 80, paint);
            }
        }
    }

    // Эффекты частиц
    private void addFireEffect(float x, float y) {
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            particles.add(new Particle(
							  x, y,
							  rand.nextFloat() * 4 - 2,
							  rand.nextFloat() * -3 - 1,
							  Color.argb(255, 255, 100, 50),
							  1.0f,
							  1.5f
						  ));
        }
    }

    private void addIceEffect(float x, float y) {
        Random rand = new Random();
        particles.add(new Particle(
						  x, y,
						  rand.nextFloat() * 3 - 1.5f,
						  rand.nextFloat() * -2 - 0.5f,
						  Color.argb(200, 100, 200, 255),
						  1.2f,
						  2.0f
					  ));
    }

    private void addLightningEffect(float x, float y) {
        Random rand = new Random();
        if (rand.nextFloat() < 0.1) {
            particles.add(new Particle(
							  x, y,
							  0, 0,
							  Color.argb(150, 200, 200, 255),
							  2.0f,
							  0.3f
						  ));
        }
    }

    private void updateParticles(float deltaTime) {
        // Простой способ без Iterator
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(deltaTime);
            if (p.isDead()) {
                particles.remove(i);
            }
        }
    }

    private void drawParticles(Canvas canvas) {
        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            p.draw(canvas, paint);
        }
    }

    // Вспомогательные методы
    private Path createStar(float x, float y, float radius, int points) {
        Path path = new Path();
        float angle = (float) (Math.PI / points);

        path.moveTo(x, y - radius);
        for (int i = 1; i < points * 2; i++) {
            float r = (i % 2 == 0) ? radius : radius / 2;
            float a = angle * i;
            path.lineTo(x + (float)Math.sin(a) * r, y - (float)Math.cos(a) * r);
        }
        path.close();
        return path;
    }

    // Класс частиц для эффектов
    class Particle {
        float x, y;
        float vx, vy;
        int color;
        float size;
        float life;
        float maxLife;

        Particle(float x, float y, float vx, float vy, int color, float size, float life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.life = life;
            this.maxLife = life;
        }

        void update(float deltaTime) {
            x += vx;
            y += vy;
            vy += 0.1f; // гравитация
            life -= deltaTime;
            size *= 0.98f;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Canvas canvas, Paint paint) {
            int alpha = (int)(255 * (life / maxLife));
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            paint.setColor(Color.argb(alpha, red, green, blue));
            canvas.drawCircle(x, y, size, paint);
        }
    }
}
