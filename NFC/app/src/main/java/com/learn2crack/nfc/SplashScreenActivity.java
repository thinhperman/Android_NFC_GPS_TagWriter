package com.learn2crack.nfc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);

        final Timer tt = new Timer();
        tt.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(intent);
                finish();

                tt.cancel();
            }
        }, 3000);


    }
}
