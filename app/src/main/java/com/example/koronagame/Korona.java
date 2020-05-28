package com.example.koronagame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Korona {

    public int speed = 20;
    public boolean wasShot = true;
    int x, y, width, height;
    Bitmap korona;

    Korona (Resources res) {
        korona = BitmapFactory.decodeResource(res, R.drawable.wirus);

        width = korona.getWidth();
        height = korona.getHeight();

        width /= 8;
        height /= 8;

        korona = Bitmap.createScaledBitmap(korona, width, height, false);

        y = -height;
    }

    Bitmap getKorona() {
        return korona;
    }

    Rect getCollisionShape () {
        return new Rect(x, y, x + width, y + height);
    }

}
