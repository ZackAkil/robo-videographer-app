package com.example.zackakil.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

/**
 * Created by zackakil on 31/12/2017.
 */

public class ImagePreProcessor {

    private float[] previousFrame;
    private int[] currentFramePixels;


    ImagePreProcessor(int frameSize){
        this.previousFrame = new float[frameSize];
    }
    

    public void feedFrame(Bitmap frame){

    }

    public float[] getLatestDeltaFrame(){

        return null;
    }

    private static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}
