package com.example.synchromusique;

import static com.example.synchromusique.tools.SendJSON;
import static com.example.synchromusique.tools.isConnexion;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;


import android.widget.Button;
import android.widget.TextView;

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
    TextView progression;

    DownloadManager downloadManager;

    private static final String TAG = "MainActivity";

    private static final int REQUEST_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBHandler dbHandler = new DBHandler(this);
        setContentView(R.layout.activity_synchromusique);

        button = (Button)findViewById(R.id.button_download);
        progression = (TextView)findViewById(R.id.progress);

        boolean connexion = isConnexion(this);
        if(!connexion){
            button.setVisibility(View.GONE);
        }

        button.setOnClickListener(new View.OnClickListener() {;
            @Override
            public void onClick(View view) {
                progression.setText("Démarrage");
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
                                String apiKey = dbHandler.get_api_key();
                                String server = dbHandler.get_server();
                                String JsonString = SendJSON(JSONMusic, server, "music_sync");
                                JSONObject jsonObject = new JSONObject(JsonString);
                                Log.d(TAG, "Mon JSON nouveau" + jsonObject);
                                Log.d(TAG, "Taille de mon JSON " + jsonObject.length());
                                Iterator<String> keys = jsonObject.keys();
                                int numberDownload = jsonObject.length() - 1;
                                while(keys.hasNext()) {
                                    String key = keys.next();
                                    Log.d(TAG, "Valeur de key" + key);
                                    Log.d(TAG, "Valeur obtenu" + jsonObject.get(key));
                                    JSONObject files = jsonObject.getJSONObject(key);
                                    String filename = files.getString(("fileName"));
                                    Log.d(TAG, "Téléchargement de " + files.getString(("fileName")));
                                    String url = "https://" + server + "/music/" + files.getString("files_URL") + "?api_key=" + apiKey;
                                    Log.d(TAG, url);
                                    //Télécharge le fichier de chaque URL
                                    String Directory_folder = files.getString("folder");
                                    if(Directory_folder.contains("/")){
                                        Directory_folder = Directory_folder.substring(0, Directory_folder.indexOf("/"));
                                    }
                                    Log.d("Test dossier", Directory_folder);
                                    Directory_folder = Directory_folder.toUpperCase();
                                    long downloadId = downloadFiles(files.getString("fileName"), url, files.getString("folder"), Directory_folder);
                                    boolean DownloadComplete = false;
                                    while(!DownloadComplete) {
                                        Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
                                        if (cursor.moveToNext()) {
                                            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                                            cursor.close();
                                            if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progression.setText("Téléchargement de " + filename + " " + key + " / "  + numberDownload);
                                                    }
                                                });
                                            }
                                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progression.setText("Téléchargement de " + filename + " " + key + " / "  + numberDownload +" réussi ! ");
                                                    }
                                                });
                                                DownloadComplete = true;

                                            }
                                            if (status == DownloadManager.STATUS_FAILED) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progression.setText("Problème lors du téléchargement du fichier :" + filename + " " + key + " / "  + numberDownload);
                                                    }
                                                });
                                                DownloadComplete = true;
                                            }

                                        }
                                        Thread.sleep(250);
                                    }
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

            // Fonction qui créé un dossier avec le chemin donné, télécharge un fichier depuis une URL donné et lui donne le nom donné.
            private long downloadFiles(String fileName, String URL, String folder, String directory_folder) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
                File musicDir = createFolder(folder, directory_folder);
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
                return downloadManager.enqueue(request);
            }

            //Fonction qui permet de créer un dossier sur le téléphone avec le chemin du dossier donné. Retourne le dossier correspondant.
            private File createFolder(String folder, String directory_folder){
                Log.d("DOSSIER", directory_folder);
                File musicDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC), "Share" + File.separator + folder);
                if (directory_folder.equals("DOCUMENTS")){
                    musicDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS),  "Share" + File.separator + folder);
                } else if (directory_folder.equals("MUSIC")) {
                    musicDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MUSIC), "Share" + File.separator + folder);
                }else if (directory_folder.equals("IMAGE")) {
                    musicDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "Share" + File.separator + folder);
                }

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
