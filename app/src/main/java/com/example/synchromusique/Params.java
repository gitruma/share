package com.example.synchromusique;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Params extends AppCompatActivity {

    private ImageView logo;
    private TextView name;
    private TextView result;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);
        this.result = (TextView) findViewById(R.id.param_result);
        this.name = (TextView) findViewById(R.id.param_name);

        button = findViewById(R.id.param_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Ecrit nom dans config.txt */
                String user = name.getText().toString();
                if (!TextUtils.isEmpty(user)) {
                    String filename = "config.txt";
                    try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
                        fos.write(user.getBytes());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    /* Charge la page de téléchargement */
                    Intent MainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(MainActivity);
                    finish();
                };
            }
        });
    }
}