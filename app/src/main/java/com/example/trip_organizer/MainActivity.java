package com.example.trip_organizer;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.SQLException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private DatabaseHelper dbHelper;
    private TripAdapter adapter;
    private ArrayList<Trip> tripList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_place);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkMusicPreference();

        listView = findViewById(R.id.listPlaceView);
        registerForContextMenu(listView);

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.open();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        loadTrips();


        listView.setOnItemClickListener((parent, view, position, id) -> {
            Trip selectedTrip = tripList.get(position);
            Intent intent = new Intent(this, ListPlaceActivity.class);
            intent.putExtra("TRIP_ID", selectedTrip.getId());
            startActivity(intent);
        });

        createNotificationChannel();
    }

    private void loadTrips() {
        if (tripList == null) {
            tripList = new ArrayList<>();
        } else {
            tripList.clear();
        }

        Cursor cursor = dbHelper.getAllTrips();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Mapping du cursor vers l'objet Place
                Trip p = new Trip(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                tripList.add(p);
            } while (cursor.moveToNext());
            cursor.close(); // Toujours fermer le cursor
        }

        adapter = new TripAdapter(this, R.layout.item_trip, tripList);
        listView.setAdapter(adapter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            NotificationChannel channel = new NotificationChannel("TRIP_CHANNEL", "Voyage", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel);
        }
    }

    private void checkMusicPreference() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean musicOn = prefs.getBoolean("MUSIC_ON", true);
        Intent intent = new Intent(this, MusicService.class);
        if (musicOn) startService(intent);
        else stopService(intent);
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
        if (id == R.id.action_add_place) {
            startActivity(new Intent(this, AddEditTripActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
            boolean currentState = prefs.getBoolean("MUSIC_ON", true);
            prefs.edit().putBoolean("MUSIC_ON", !currentState).apply();
            checkMusicPreference();
            Toast.makeText(this, currentState ? "Musique OFF" : "Musique ON", Toast.LENGTH_SHORT).show();
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
        Trip selectedTrip = tripList.get(info.position);

        if (item.getItemId() == R.id.action_delete_place) {
            dbHelper.deleteTrip(selectedTrip.getId());
            loadTrips(); // Recharger la liste
            Toast.makeText(this, "Voyage supprimé", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_edit_place) {
            Intent intent = new Intent(this, AddEditTripActivity.class);
            intent.putExtra("TRIP_OBJECT", selectedTrip); // Grâce à Parcelable
            startActivity(intent);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips(); // Rafraîchir quand on revient d'un ajout/modif
    }
}