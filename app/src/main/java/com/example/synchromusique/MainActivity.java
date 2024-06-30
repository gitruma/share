package com.example.synchromusique;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String filename = "config.txt";
        File file = new File(getApplicationContext().getFilesDir(),filename);
        if(!file.exists()) {
            Intent Params = new Intent(getApplicationContext(), SynchroMusique.class);
            startActivity(Params);
            finish();
        } else {
            Intent SynchroMusique = new Intent(getApplicationContext(), SynchroMusique.class);
            startActivity(SynchroMusique);
            finish();
        }
    }
    }
