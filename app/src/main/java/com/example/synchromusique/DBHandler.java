package com.example.synchromusique;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "hiruma";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String TABLE_NAME = "connexion";

    // below variable is for our id column.
    private static final String ID = "id";

    private static final String SERVER = "server";

    private static final String API_KEY = "api_key";

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Vérifier si création avant
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + API_KEY + " TEXT, " + SERVER + " TEXT)";

        db.execSQL(query);
    }

    public void addData(String api_key_string, String server_string) {

        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        SQLiteDatabase db = this.getWritableDatabase();

        // on below line we are creating a
        // variable for content values.
        ContentValues values = new ContentValues();

        // on below line we are passing all values
        // along with its key and value pair.
        values.put(API_KEY, api_key_string);
        values.put(SERVER, server_string);

        // after adding all values we are passing
        // content values to our table.
        db.insert(TABLE_NAME, null, values);

        // at last we are closing our
        // database after adding database.
        db.close();
    }

    public String get_api_key() {
        SQLiteDatabase db=this.getReadableDatabase();
        String apikey = null;
        String query = "SELECT API_KEY FROM connexion LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            apikey = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return apikey;
    }

    public String get_server() {
        SQLiteDatabase db=this.getReadableDatabase();
        String server = null;
        String query = "SELECT SERVER FROM connexion LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            server = cursor.getString(0); // Récupère la valeur de la première colonne
        }

        cursor.close();
        db.close();
        return server;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
