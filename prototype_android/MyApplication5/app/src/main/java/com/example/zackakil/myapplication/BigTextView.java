package com.example.zackakil.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BigTextView extends AppCompatActivity {

    private BluetoothTripod mBluetoothTripod;
    private SharedPreferences sharedPrefs;

    private int minPos;
    private int maxPos;
    private int latestPos;

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
        mBluetoothTripod.close();

    }

    private void sendPos(){

        int val = scalePosValue(latestPos/100.0f);
        sendDownBluetooth((char) val);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_text_view);


         sharedPrefs =  BigTextView.this.getSharedPreferences(
                "my apps prefs", Context.MODE_PRIVATE
        );


        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progresValue, boolean fromUser) {

                        latestPos = progresValue;
                        sendPos();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });


        SeekBar seekBarRight = (SeekBar) findViewById(R.id.seekBarRight);

        int pos_r = sharedPrefs.getInt( getString(R.string.right_boundary_key), 100);
        seekBarRight.setProgress(pos_r);
        maxPos = pos_r;

        seekBarRight.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progresValue, boolean fromUser) {

                        maxPos = progresValue;
                        sendPos();
                        SharedPreferences.Editor editor = sharedPrefs.edit();

                        editor.putInt(getString(R.string.right_boundary_key), progresValue);
                        editor.commit();

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });


        SeekBar seekBarLeft = (SeekBar) findViewById(R.id.seekBarLeft);


        int pos_l = sharedPrefs.getInt( getString(R.string.left_boundary_key), 0);
        seekBarLeft.setProgress(pos_l);
        minPos = pos_l;

        seekBarLeft.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progresValue, boolean fromUser) {


                        minPos = progresValue;
                        sendPos();
                        SharedPreferences.Editor editor = sharedPrefs.edit();

                        editor.putInt(getString(R.string.left_boundary_key), progresValue);
                        editor.commit();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });


        mBluetoothTripod = new BluetoothTripod("HC-06");

    }


    private int scalePosValue(float val){

        int delta = maxPos - minPos;
        int out = (int)(delta * val) + minPos;
        return out;


    }



    public void sendStuffOnBluetooh(View view){
        /**
         * Send somethiing to arduino using bluetooth
         */
        EditText editText = (EditText) findViewById(R.id.bthEditText);
        String message = editText.getText().toString();
        Log.d("My App", message );

        sendDownBluetooth(message.getBytes());
    }

    private void sendDownBluetooth(byte[] bytes) {
        mBluetoothTripod.sendDownBluetooth(bytes);
    }

    private void sendDownBluetooth(char bytes) {
        mBluetoothTripod.sendDownBluetooth(bytes);
    }

}
