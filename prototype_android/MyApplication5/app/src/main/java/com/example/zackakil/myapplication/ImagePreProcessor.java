package com.example.zackakil.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by zackakil on 31/12/2017.
 */

public class ImagePreProcessor {

    private float[] previousFrameFloatPixels;
    private int[] currentFramePixels;
    private float[] currentFrameFloatPixels;
    private float[] deltaPixels;
    private int frameSize;
    private int frameHeight;
    private int frameWidth;
    private Bitmap deltaBitmap;



    ImagePreProcessor(int frameWidth, int frameHeight){

        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameSize = this.frameWidth * this.frameHeight;

        this.currentFramePixels = new int[this.frameSize];
        this.deltaPixels = new float[this.frameSize];
        this.currentFrameFloatPixels = new float[this.frameSize];
        this.deltaBitmap = Bitmap.createBitmap(this.frameWidth, this.frameHeight,
                                                Bitmap.Config.ARGB_8888);
    }


    /**
     * @param frame un-processed frame from camera
     *
     */
    public void feedFrame(Bitmap frame){

        final Bitmap newRgbFrameBitmap = Bitmap.createScaledBitmap(
                                                toGrayscale(frame),
                                                this.frameWidth, this.frameHeight, false);

        newRgbFrameBitmap.getPixels(this.currentFramePixels, 0, this.frameWidth, 0, 0,
                                        this.frameWidth, this.frameHeight);

        this.previousFrameFloatPixels = this.currentFrameFloatPixels.clone();

        for(int i = 0; i<this.frameSize; i++){
            this.currentFrameFloatPixels[i] = ((float) Color.red(this.currentFramePixels[i])) / 255.f ;
        }

    }

    public float[] getLatestDeltaFrame(){

        if (this.previousFrameFloatPixels == null)
            return null;

        for(int i = 0; i < this.frameSize; i++){
            this.deltaPixels[i] = Math.max(this.currentFrameFloatPixels[i] - this.previousFrameFloatPixels[i], 0);
        }

        return this.deltaPixels;
    }

    public Bitmap getLatestDeltaFrameAsBitmap(){

        this.deltaBitmap.setPixels(floatArray2IntArray(this.deltaPixels),
                                    0, this.frameWidth, 0, 0, this.frameWidth, this.frameHeight);

        return this.deltaBitmap;
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

//    private float[] getDeltaPixels(float[] frame1, float[] frame2){
//
//
//        float sum = 0;
//        for(int i = 0; i<this.frameSize; i++){
//            this.deltaPixels[i] = Math.max(frame1[i] - frame2[i], 0);
//            sum += Math.max(frame1[i] - frame2[i], 0);
//        }
//
//        Log.d("My App", "sum ="+ sum);
//        return this.deltaPixels;
//    }

    private static int [] floatArray2IntArray (float[] values)
    {
        int[] out = new int[values.length];

        for (int i =0; i<values.length; i++){
            int c =  (int) Math.min(values[i]*255, 255);
            out[i] = Color.rgb(c, c, c);
        }

        return out;
    }
}
