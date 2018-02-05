package com.example.zackakil.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zackakil on 05/02/2018.
 */

public class SignalScaler {

    private SharedPreferences sharedPrefs;
    private int minPos;
    private int maxPos;

    SignalScaler(Context ctx){

        sharedPrefs =  ctx.getSharedPreferences(
                "my apps prefs", Context.MODE_PRIVATE
        );

        maxPos = sharedPrefs.getInt( ctx.getString(R.string.right_boundary_key), 100);
        minPos = sharedPrefs.getInt( ctx.getString(R.string.left_boundary_key), 0);

    }

    public float scaleVal(float val){

        int delta = maxPos - minPos;
        int out = (int)(delta * val) + minPos;
        return out/100.0f;
    }
}
