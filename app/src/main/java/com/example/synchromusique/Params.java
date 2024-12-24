package com.example.synchromusique;

import static com.example.synchromusique.tools.SendJSON;
import static com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ComparisonChain.start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Params extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private EditText server;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DBHandler dbHandler = new DBHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);
        username = findViewById(R.id.username_input);
        password = findViewById(R.id.password_input);
        server = findViewById(R.id.server_input);
        button = findViewById(R.id.param_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String pass = password.getText().toString();
                String serverAddress = server.getText().toString();
                String deviceName = Build.MODEL;
                Log.d("Device", "Device" + user);
                Log.d("Device", "Device" + deviceName);
                JSONObject json = new JSONObject();
                try {
                    json.put("username", user);
                    json.put("password", pass);
                    json.put("device", deviceName);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String ApiKEY = SendJSON(json, serverAddress, "api_connexion_user");
                            Log.d("ApiKEY", "API " + ApiKEY);
                            if (!ApiKEY.equals("False")) {
                                dbHandler.addData(ApiKEY, serverAddress);
                                Intent SynchroMusique = new Intent(getApplicationContext(), SynchroMusique.class);
                                startActivity(SynchroMusique);
                                finish();
                            }
                        }catch (Exception e) {
                            Log.d("Erreur", e.toString());
                        }
                    }}).start();
                //TODO : Requête vers Hiruma : api_connexion_user avec en paramètre le user, le pass, et le server
                //Récupérer APIKEY et le mettre dans la DB avec le serveur.
                //Si tout est ok, retourne activité Synchro, et juste rajouter ApiKey to synchro.
                Intent MainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(MainActivity);

                    finish();
                };
        });
    }
}