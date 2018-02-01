package com.example.zackakil.myapplication;

/**
 * Created by zackakil on 01/02/2018.
 */

public class SignalFeedDampener {

    public double dampenConstant = 0.15;
    private double previousValue = 0;
    private double residual;
    private double weight;
    private double addition;
    private double nextValue;

    public SignalFeedDampener(){}


    public double getNextVal(float feedValue){

        residual = feedValue - previousValue;
        weight = Math.pow(2, -Math.abs(residual/dampenConstant));
        addition = weight * residual;
        nextValue = previousValue + addition;

        previousValue = nextValue;
        return nextValue;
    }
}
