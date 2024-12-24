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
        DBHandler dbHandler = new DBHandler(this);
        String apiKey = dbHandler.get_api_key();
        String server = dbHandler.get_server();
        if (apiKey != null && server != null){
            Intent SynchroMusique = new Intent(getApplicationContext(), SynchroMusique.class);
            startActivity(SynchroMusique);
            finish();
        } else {
            Intent Params = new Intent(getApplicationContext(), Params.class);
            startActivity(Params);
            finish();

        }
    }
    }
