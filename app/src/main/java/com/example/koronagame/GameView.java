package com.example.koronagame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    // two instances of background class
    private Background background1, background2;
    private GameActivity gameActivity;
    private Paint paint;
    private Korona[] koronas;
    private SharedPreferences prefs;
    private List<Bullet> bullets;
    private Flight flight;
    // size of the screen
    private int screenX, screenY, score = 0;
    private Random random;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.gameActivity = activity;

        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

        this.screenX = screenX;
        this.screenY = screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        flight = new Flight(this, screenY, getResources());

        bullets = new ArrayList<>();

        background2.x = screenX;

        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        koronas = new Korona[4];

        for (int i = 0; i < 4; i++) {
            Korona korona = new Korona(getResources());
            koronas[i] = korona;
        }

        random = new Random();
    }

    @Override
    public void run() {

        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    // change position of  background on x
    private void update() {
        // move by 10 px on left
        background1.x -= 10;
        background2.x -= 10;

        //  check if background if completely of the screen
        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }
        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }
        if (flight.isGoingUp)
            flight.y -= 30;
        else
            flight.y += 30;

        if (flight.y < 0)
            flight.y = 0;

        if (flight.y > screenY - flight.height)
            flight.y = screenY - flight.height;

        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : bullets) {
            if (bullet.x > screenX) {
                trash.add(bullet);
            }
            bullet.x += 50;

            for (Korona korona : koronas) {
                if (Rect.intersects(korona.getCollisionShape(), bullet.getCollisionShape())) {
                    score++;
                    korona.x = -500;
                    bullet.x = screenX + 500;
                    korona.wasShot = true;
                }
            }
        }

        for (Bullet bullet : trash) {
            bullets.remove(bullet);
        }

        for (Korona korona : koronas) {
            korona.x -= korona.speed;

            if (korona.x + korona.width < 0) {

                if (!korona.wasShot) {
                    isGameOver = true;
                    return;
                }

                int bound = 30;
                korona.speed = random.nextInt(bound);

                if (korona.speed < 15)
                    korona.speed = 15;

                korona.x = screenX;
                korona.y = random.nextInt(screenY - korona.height);

                korona.wasShot = false;
            }

            if (Rect.intersects(korona.getCollisionShape(), flight.getCollisionShape())) {

                isGameOver = true;
                return;
            }
        }

    }

    private void draw() {
        // check if surface is valid if no return null object
        if (getHolder().getSurface().isValid()) {

            // locking the canvas
            Canvas canvas = getHolder().lockCanvas();
            // drawing a background
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            for (Korona korona : koronas)
                canvas.drawBitmap(korona.getKorona(), korona.x, korona.y, paint);

            canvas.drawText(score + "", screenX / 2f, 164, paint);

            // end Game
            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting();
                return;
            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            for (Bullet bullet : bullets) {
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);
            }

            // unlocking the canvas
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void waitBeforeExiting() {
        try {
            Thread.sleep(3000);
            gameActivity.startActivity(new Intent(gameActivity, MainActivity.class));
            gameActivity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveIfHighScore() {
        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {

        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2) {
                    flight.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                flight.isGoingUp = false;
                if (event.getX() > screenX / 2)
                    flight.toShoot++;
                break;
        }

        return true;
    }

    public void newBullet() {
        Bullet bullet = new Bullet(getResources());
        bullet.x = flight.x + flight.width;
        bullet.y = flight.y + (flight.height / 2);
        bullets.add(bullet);
    }
}
