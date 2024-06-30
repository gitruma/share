package com.example.synchromusique;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;


import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class SynchroMusique extends AppCompatActivity {

    Button button;
    DownloadManager downloadManager;

    private static final String TAG = "MainActivity";

    private static final int REQUEST_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchromusique);

        button = (Button)findViewById(R.id.button_download);
        button.setOnClickListener(new View.OnClickListener() {;
            @Override
            public void onClick(View view) {
                /* Crée le dossier Game Music si il n'existe pas*/
                File musicDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC), "Game Music");
                if (!musicDir.exists()) {
                    musicDir.mkdirs();
                }
                /* Création de la liste des fichiers dans un JSON puis envoi du JSON vers le serveur*/
                try {
                    JSONArray JSONMusic = JsonFile(musicDir);
                    Log.d("Files", "Voici mon JSON" + JSONMusic);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Récupère le JSON que le serveur retourne
                                String JsonString = SendJSON(JSONMusic);
                                JSONObject jsonObject = new JSONObject(JsonString);
                                Log.d(TAG, "Mon JSON nouveau" + jsonObject);
                                Log.d(TAG, "Taille de mon JSON " + jsonObject.length());
                                Iterator<String> keys = jsonObject.keys();
                                while(keys.hasNext()) {
                                    String key = keys.next();
                                    Log.d(TAG, "Valeur de key" + key);
                                    Log.d(TAG, "Valeur obtenu" + jsonObject.get(key));
                                    JSONObject files = jsonObject.getJSONObject(key);
                                    //Télécharge le fichier de chaque URL
                                    downloadFiles(files.getString("fileName"), files.getString("URL"), files.getString("folder"));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        }).start();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            // Fonction JsonFile qui répértorie tout les fichiers d'une arborescence et renvoie un Json
            private JSONArray JsonFile(File directory) throws JSONException {
                JSONArray jsonArray = new JSONArray();
                if (directory.exists() && directory.isDirectory()){
                    Log.d("Files", "Checking directory: " + directory.getAbsolutePath());
                    File[] files = directory.listFiles();
                    Log.d("Files", Arrays.toString(files));
                    if (files != null) {
                        for (File file : files){
                            byte[] bytes = file.getName().getBytes(StandardCharsets.UTF_8);
                            String Name = new String(bytes, StandardCharsets.UTF_8);
                            Log.d("Files", "FileName" + Name);
                            if(file.isDirectory()){
                                jsonArray.put(JsonFile(file));
                            }else{
                                JSONObject jsonObject = new JSONObject();
                                Log.d(TAG, "Nom fichier " + Name);
                                jsonObject.put("fichier", Name);
                                jsonArray.put(jsonObject);
                                Log.d("File", "FileName" + jsonObject);
                            }
                        }
                    }
                }else{
                    Log.d("Files", "Le chemin n'existe pas où n'est pas un dossier");
                }
                Log.d(TAG, "Mon JSON magnifique" + jsonArray);
                return jsonArray;
            }
            // Fonction qui permet d'envoyer un JSON vers le serveur WEB
            private String SendJSON(JSONArray json) {
                try {
                    Log.d(TAG, "json" + json);
                    URL url = new URL("http://192.168.1.142:5000/music_sync");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
                    String json_files = new String(bytes, StandardCharsets.UTF_8);
                    os.write(json_files.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    os.close();
                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());
                    BufferedReader br = null;
                    StringBuilder builder = new StringBuilder();
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String strCurrentLine;
                    while ((strCurrentLine = br.readLine()) != null) {
                        builder.append(strCurrentLine);
                    }
                    conn.disconnect();
                    return builder.toString();
                } catch (Exception e){
                    Log.e(TAG, "Error in sending JSON", e);
                }
                return null;
            }

            // Fonction qui créé un dossier avec le chemin donné, télécharge un fichier depuis une URL donné et lui donne le nom donné.
            private void downloadFiles(String fileName, String URL, String folder) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
                File musicDir = createFolder(folder);
                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                File file = new File(musicDir, fileName);
                Log.d(TAG, URL);
                Uri uri = Uri.parse(URL);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setAllowedOverMetered(true);
                request.setAllowedOverRoaming(true);
                request.setDestinationUri(Uri.fromFile(file));
                Long reference = downloadManager.enqueue(request);
            }

            //Fonction qui permet de créer un dossier sur le téléphone avec le chemin du dossier donné. Retourne le dossier correspondant.
            private File createFolder(String folder){
                File musicDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC), "Game Music" + File.separator + folder);
                if (!musicDir.exists()) {
                    if (musicDir.mkdirs()) {
                        Log.d("Test", "Dossier créé avec succès: " + musicDir.getAbsolutePath());
                    }else{
                        Log.e("Erreur", "Échec de la création du dossier: " + musicDir.getAbsolutePath());
                    }
                }else{
                    Log.d("Test", "Le dossier existe déjà: " + musicDir.getAbsolutePath());
                }
                return musicDir;


            }
        });
    }

}
