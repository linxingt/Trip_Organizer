package com.example.trip_organizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLException;
import java.util.ArrayList;

public class ListPlaceActivity extends AppCompatActivity {
    private ListView listView;
    private DatabaseHelper dbHelper;
    private PlaceAdapter adapter;
    private ArrayList<Place> placeList;
    private long tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_place);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_list_place);
        }

        // Récupérer le TRIP_ID passé par Intent
        tripId = getIntent().getLongExtra("TRIP_ID", -1);
        listView = findViewById(R.id.listPlaceView);
        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.open();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        loadPlaces();
        // Enregistrer le menu contextuel sur la liste
        registerForContextMenu(listView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Place selectedPlace = placeList.get(position);
            Intent intent = new Intent(this, AddEditPlaceActivity.class);
            intent.putExtra("PLACE_OBJECT", selectedPlace);
            intent.putExtra("MODE", "DETAIL");
            startActivity(intent);
        });
    }

    private void loadPlaces() {
        if (placeList == null) {
            placeList = new ArrayList<>();
        } else {
            placeList.clear(); // On vide la liste avant de la recharger
        }

        tripId = getIntent().getLongExtra("TRIP_ID", -1);
        Cursor cursor = dbHelper.getAllPlacesByTripId(tripId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Mapping du cursor vers l'objet Place
                Place p = new Place(
                        cursor.getLong(0), cursor.getLong(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5),
                        cursor.getString(6), cursor.getString(7), cursor.getString(8),
                        cursor.getInt(9)
                );
                placeList.add(p);
            } while (cursor.moveToNext());
            cursor.close(); // Toujours fermer le cursor
        }

        adapter = new PlaceAdapter(this, R.layout.item_place, placeList);
        listView.setAdapter(adapter);
    }

    // --- Menu d'options ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        tripId = getIntent().getLongExtra("TRIP_ID", -1);
        if (id == R.id.action_add_place) {
            Intent intent = new Intent(this, AddEditPlaceActivity.class);
            intent.putExtra("TRIP_ID", tripId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
            boolean isMusicOn = prefs.getBoolean("MUSIC_ON", true);

            boolean newState = !isMusicOn;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("MUSIC_ON", newState);
            editor.apply();

            Intent intent = new Intent(this, MusicService.class);
            if (newState) {
                startService(intent);
                Toast.makeText(this, "Musique ON", Toast.LENGTH_SHORT).show();
            } else {
                stopService(intent);
                Toast.makeText(this, "Musique OFF", Toast.LENGTH_SHORT).show();
            }
            return true;
        }else if (id == R.id.action_notif) {
            String[] options = {getString(R.string.popup_notif7),getString(R.string.popup_notif2)};
            SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
            String current = prefs.getString("NOTICE_DELAY", "7");

            int checkedItem = current.equals("7") ? 0 : 1;

            new AlertDialog.Builder(this)
                    .setTitle("Préférence Notification")
                    .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                        String value = (which == 0) ? "7" : "2";
                        prefs.edit().putString("NOTICE_DELAY", value).apply();

                        Toast.makeText(this,
                                "Notification " + value + " jours avant",
                                Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    })
                    .setNegativeButton(getString(R.string.menu_cancel), null)
                    .show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Menu Contextuel ---
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.item_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Place selectedPlace = placeList.get(info.position);

        if (item.getItemId() == R.id.action_delete_place) {
            dbHelper.deletePlace(selectedPlace.getId());
            loadPlaces(); // Recharger la liste
            return true;
        } else if (item.getItemId() == R.id.action_edit_place) {
            Intent intent = new Intent(this, AddEditPlaceActivity.class);
            intent.putExtra("PLACE_OBJECT", selectedPlace); // Grâce à Parcelable
            intent.putExtra("MODE", "EDIT");
            startActivity(intent);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces(); // Rafraîchir quand on revient d'un ajout/modif
    }
}