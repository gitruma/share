package com.example.synchromusique;

import static com.example.synchromusique.tools.SendJSON;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBHandler dbHandler = new DBHandler(this);
        String apiKey = dbHandler.get_api_key();
        String server = dbHandler.get_server();
        if (apiKey != null && server != null){
            String deviceName = Build.MODEL;
            JSONObject json = new JSONObject();
            try {
                json.put("apikey", apiKey);
                json.put("device", deviceName);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
            String result = SendJSON(json, server, "test_api");
            if(Objects.equals(result, "True")){
                Intent SynchroMusique = new Intent(getApplicationContext(), SynchroMusique.class);
                startActivity(SynchroMusique);
                finish();
            } else if (Objects.equals(result, "False")) {
                Log.d("Mauvaise clé d'API", "Mauvaise clé d'API, merci de la corriger.");
                Intent Params = new Intent(getApplicationContext(), Params.class);
                startActivity(Params);
                finish();
            }else {
                Log.d("Serveur down", "Serveur down, merci d'essayer plus tard :)");
                Intent Params = new Intent(getApplicationContext(), Params.class);
                startActivity(Params);
                finish();
            }
                    }catch (Exception e) {
                        Log.d("Erreur", e.toString());
                    }
                }}).start();
        } else {
            Intent Params = new Intent(getApplicationContext(), Params.class);
            startActivity(Params);
            finish();

        }
    }
    }
