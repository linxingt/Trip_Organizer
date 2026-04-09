package com.example.trip_organizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase database;

    // Table et Colonnes
    // Table Voyage
    public static final String TABLE_TRIP = "TRIP";
    public static final String TRIP_ID = "_id";
    public static final String TRIP_CITY = "city";
    public static final String TRIP_START = "start_date";
    public static final String TRIP_END = "end_date";

    // Table Lieu (Place)
    public static final String TABLE_PLACE = "PLACE";
    public static final String PLACE_ID = "_id";
    public static final String PLACE_TRIP_ID = "trip_id"; // Clé étrangère
    public static final String PLACE_TITLE = "title";
    public static final String PLACE_DESC = "description";
    public static final String PLACE_DATE = "date";
    public static final String PLACE_HOUR = "hour";
    public static final String PLACE_ADDRESS = "address";
    public static final String PLACE_PHONE = "phone";
    public static final String PLACE_PHOTO = "photo";
    public static final String PLACE_STATUS = "is_visited"; // 0 pour faux, 1 pour vrai

    // Infos BDD
    static final String DB_NAME = "trip_planner.db";
    static final int DB_VERSION = 1;
    // Creating table query
    private static final String CREATE_TABLE_TRIP = "CREATE TABLE " + TABLE_TRIP + "(" + TRIP_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TRIP_CITY + " TEXT NOT NULL, " + TRIP_START + " TEXT, "
            + TRIP_END + " TEXT);";
    private static final String CREATE_TABLE_PLACE = "CREATE TABLE " + TABLE_PLACE + "(" + PLACE_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PLACE_TRIP_ID + " INTEGER, " + PLACE_TITLE + " TEXT NOT NULL, "
            + PLACE_DESC + " TEXT, " + PLACE_DATE + " TEXT, " + PLACE_HOUR + " TEXT, " + PLACE_ADDRESS + " TEXT, "
            + PLACE_PHONE + " TEXT, " + PLACE_PHOTO + " TEXT, " + PLACE_STATUS + " INTEGER DEFAULT 0," +
            "FOREIGN KEY(" + PLACE_TRIP_ID + ") REFERENCES " + TABLE_TRIP + "(" + TRIP_ID
            + ") ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Activer les clés étrangères pour le ON DELETE CASCADE
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TRIP);
        db.execSQL(CREATE_TABLE_PLACE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP);
        onCreate(db);
    }

    public void open() throws SQLException {
        database = this.getWritableDatabase();
    }

    public void deleteAllTrips() {
        database.delete(TABLE_TRIP, null, null);
    }

    public void deleteAllPlaces() {
        database.delete(TABLE_PLACE, null, null);
    }

    public void close() {
        database.close();
    }

    public boolean canEditTrip(long tripId) {
        Cursor cursor = getAllPlacesByTripId(tripId);
        boolean canEdit = (cursor.getCount() == 0);
        cursor.close();
        return canEdit;
    }

    // --- OPERATIONS CRUD ---

    // 1. Ajouter (Create)
    public long addTrip(Trip trip) {
        ContentValues values = new ContentValues();
        values.put(TRIP_CITY, trip.getCity());
        values.put(TRIP_START, trip.getStartDate());
        values.put(TRIP_END, trip.getEndDate());
        return database.insert(TABLE_TRIP, null, values);
    }

    public long addPlace(Place place) {
        ContentValues values = new ContentValues();
        values.put(PLACE_TRIP_ID, place.getTripId());
        values.put(PLACE_TITLE, place.getTitle());
        values.put(PLACE_DESC, place.getDescription());
        values.put(PLACE_DATE, place.getDate());
        values.put(PLACE_HOUR, place.getHour());
        values.put(PLACE_ADDRESS, place.getAddress());
        values.put(PLACE_PHONE, place.getPhone());
        values.put(PLACE_PHOTO, place.getPhoto());
        values.put(PLACE_STATUS, place.getIsVisited());
        return database.insert(TABLE_PLACE, null, values);
    }

    // 2. Mettre à jour (Update)
    public int updateTrip(Trip trip) {
        ContentValues values = new ContentValues();
        values.put(TRIP_CITY, trip.getCity());
        values.put(TRIP_START, trip.getStartDate());
        values.put(TRIP_END, trip.getEndDate());
        return database.update(TABLE_TRIP, values, TRIP_ID + " = ?", new String[]{String.valueOf(trip.getId())});
    }

    public int updatePlace(Place place) {
        ContentValues values = new ContentValues();
        values.put(PLACE_TRIP_ID, place.getTripId());
        values.put(PLACE_TITLE, place.getTitle());
        values.put(PLACE_DESC, place.getDescription());
        values.put(PLACE_DATE, place.getDate());
        values.put(PLACE_HOUR, place.getHour());
        values.put(PLACE_ADDRESS, place.getAddress());
        values.put(PLACE_PHONE, place.getPhone());
        values.put(PLACE_PHOTO, place.getPhoto());
        values.put(PLACE_STATUS, place.getIsVisited());
        return database.update(TABLE_PLACE, values, PLACE_ID + " = ?", new String[]{String.valueOf(place.getId())});
    }

    // 3. Lire tout (Read)
    public Cursor getAllTrips() {
        String[] projection = {TRIP_ID, TRIP_CITY, TRIP_START, TRIP_END};
        Cursor cursor = database.query(TABLE_TRIP, projection, null, null, null, null, null, null);
        return cursor;
    }

    // Récupérer un Trip par son ID
    public Trip getTripById(long id) {
        Cursor cursor = database.query(TABLE_TRIP, null, TRIP_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        Trip trip = null;
        if (cursor != null && cursor.moveToFirst()) {
            trip = new Trip(
                    cursor.getLong(cursor.getColumnIndexOrThrow(TRIP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TRIP_CITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TRIP_START)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TRIP_END))
            );
            cursor.close();
        }
        return trip;
    }

    // Récupérer un Place par son ID
    public Place getPlaceById(long id) {
        Cursor cursor = database.query(TABLE_PLACE, null, PLACE_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        Place place = null;
        if (cursor != null && cursor.moveToFirst()) {
            place = new Place(
                    cursor.getLong(cursor.getColumnIndexOrThrow(PLACE_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(PLACE_TRIP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PLACE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PLACE_DESC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PLACE_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PLACE_HOUR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PLACE_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PLACE_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PLACE_PHOTO)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(PLACE_STATUS))
            );
            cursor.close();
        }
        return place;
    }

    // Récupérer tous les lieux (Places) d'un voyage (Trip) spécifique
    public Cursor getAllPlacesByTripId(long tripId) {
        String[] projection = {PLACE_ID, PLACE_TRIP_ID, PLACE_TITLE, PLACE_DESC, PLACE_DATE, PLACE_HOUR, PLACE_ADDRESS,
                PLACE_PHONE, PLACE_PHOTO, PLACE_STATUS};
        return database.query(TABLE_PLACE, projection, PLACE_TRIP_ID + " = ?", new String[]{String.valueOf(tripId)}, null, null, null);
    }


    public Cursor getAllPlaces() {
        String[] projection = {PLACE_ID, PLACE_TRIP_ID, PLACE_TITLE, PLACE_DESC, PLACE_DATE, PLACE_HOUR, PLACE_ADDRESS,
                PLACE_PHONE, PLACE_PHOTO, PLACE_STATUS};
        Cursor cursor = database.query(TABLE_PLACE, projection, null, null, null, null, null, null);
        return cursor;
    }

    // 4. Supprimer (Delete)
    public void deleteTrip(long id) {
        database.delete(TABLE_TRIP, TRIP_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deletePlace(long id) {
        database.delete(TABLE_PLACE, PLACE_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
