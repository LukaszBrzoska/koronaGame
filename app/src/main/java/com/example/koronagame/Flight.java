package com.example.koronagame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Flight {

    public boolean isGoingUp = false;
    public int toShoot = 0;
    int x, y, width, height, wingCounter = 0, shootCounter = 1;
    Bitmap flight;
    private GameView gameView;

    Flight (GameView gameView, int screenY, Resources res) {

        this.gameView = gameView;

        flight = BitmapFactory.decodeResource(res, R.drawable.ship);

        width = flight.getWidth();
        height = flight.getHeight();

        width /= 2;
        height /= 2;

        flight = Bitmap.createScaledBitmap(flight, width, height, false);

        y = screenY / 2;
        x = 64;
    }

    //draw fly
    Bitmap getFlight() {

        if (toShoot != 0) {
            shootCounter = 1;
            toShoot--;
            gameView.newBullet();
        }

        return flight;
    }

    Rect getCollisionShape () {
        return new Rect(x, y, x + width, y + height);
    }
}
