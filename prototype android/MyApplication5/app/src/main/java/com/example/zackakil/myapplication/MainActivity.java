package com.example.zackakil.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.zackakil.myapplication.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Context context = MainActivity.this;
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                getString(R.string.pref_key), Context.MODE_PRIVATE
        );
        String savedMsg = sharedPrefs.getString( getString(R.string.pref_key), "Hello");
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText(savedMsg);

    }

    public void sendMessage(View view){

        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);

        Context context = MainActivity.this;
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                getString(R.string.pref_key), Context.MODE_PRIVATE
        );

        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(getString(R.string.pref_key), message + "_s");
        editor.commit();


        startActivity(intent);
    }


    public void saveBigText(View wiew){

        EditText editText = (EditText) findViewById((R.id.editText2));
        String message = editText.getText().toString();

        Context context = getApplicationContext();
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

//        File file = new File(context.getFilesDir(), filename);

        Log.d("My App", context.getFilesDir().toString() );


        String filename = "myfile";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(message.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, BigTextView.class);
        startActivity(intent);

    }
}
