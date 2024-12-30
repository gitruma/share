package com.example.synchromusique;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class tools {

    /*
    Fonction qui permet d'envoyer un JSON à un serveur web
     */
    public static String SendJSON(Object json, String server, String route) {
        try {
            Log.d(TAG, "json" + json);
            URL url = new URL("https://" + server  + "/" + route);
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
    /*
    Fonction qui permet de tester la connexion internet d'un téléphone
     */
    public static boolean isConnexion(Context context) {
        ConnectivityManager connectivityManager =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


