package com.example.trip_organizer;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditTripActivity extends AppCompatActivity {
    private EditText etCity, etStartDate, etEndDate;
    private Button btnSaveTrip, btnPickStart, btnPickEnd;
    private DatabaseHelper dbHelper;

    private boolean isEndDate;
    private int yearS, monthS, dayS;
    private int yearE, monthE, dayE;
    private EditText currentTargetField; // Pour savoir quel champ de date on remplit

    private boolean isEditMode = false;
    private Trip currentTrip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_trip);// Lie cette classe au fichier XML

        // Initialisation de la base de données
        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.open();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Liaison des éléments graphiques (Views) par leurs IDs
        etCity = findViewById(R.id.et_city);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        btnSaveTrip = findViewById(R.id.btn_save_trip);
        btnPickStart = findViewById(R.id.btn_pick_start);
        btnPickEnd = findViewById(R.id.btn_pick_end);
        TextView tvTitle = findViewById(R.id.tv_title);


        // Vérifier si on reçoit un Trip pour modification
        if (getIntent().hasExtra("TRIP_OBJECT")) {
            isEditMode = true;
            currentTrip = getIntent().getParcelableExtra("TRIP_OBJECT");
            tvTitle.setText(R.string.title_edit_trip);
            etCity.setText(currentTrip.getCity());
            etStartDate.setText(currentTrip.getStartDate());
            etEndDate.setText(currentTrip.getEndDate());

            // Règle métier : Bloquer si des lieux existent déjà
            if (currentTrip != null && !dbHelper.canEditTrip(currentTrip.getId())) {
                etCity.setEnabled(false);
                btnPickStart.setEnabled(false);
                btnPickEnd.setEnabled(false);
                btnSaveTrip.setEnabled(false);
                Toast.makeText(this, "Modif. impossible : des lieux sont liés", Toast.LENGTH_LONG).show();
            } else btnSaveTrip.setText(R.string.menu_edit);

        } else {
            tvTitle.setText(R.string.title_create_trip);
        }
        // Actions des boutons "Pick"
        btnPickStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTargetField = etStartDate;
                showDatePicker("start");
            }
        });

        btnPickEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTargetField = etEndDate;
                showDatePicker("end");
            }
        });

        // Action du bouton "Enregistrer"
        btnSaveTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrip();
            }
        });
    }

    // Méthode pour afficher le DatePickerFragment (selon le cours)
    private void showDatePicker(String type) {
        boolean isEnd = type.equals("end");

        // 1. Récupérer la date du jour
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // 2. Préparer les arguments pour le fragment
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);

        // 3. Créer et configurer le fragment
        DatePickerFragment date = new DatePickerFragment();
        date.setArguments(args);

        date.setCallBack(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // On ajoute +1 au mois car Calendar.MONTH commence à 0
                String dateSelected = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                currentTargetField.setText(dateSelected);
            }
        });

        date.show(getSupportFragmentManager(), isEnd ? "End Date Picker" : "Start Date Picker");
    }

    private void saveTrip() {
        String city = etCity.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();

        // Règle métier : La ville ne doit pas être vide
        if (city.isEmpty()) {
            Toast.makeText(this, "Les ville est obligatoire", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Les dates sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);

                if (start != null && end != null && start.after(end)) {
                    Toast.makeText(this, "La date de début doit être avant la date de fin", Toast.LENGTH_LONG).show();
                    return; // On stoppe l'enregistrement
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Format de date invalide", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        long tripId = -1;

        if (currentTrip == null) {
            // Création de l'objet Trip
            Trip newTrip = new Trip(city, startDate, endDate);
            // Ajout dans la base de données
            tripId = dbHelper.addTrip(newTrip);
        } else {
            currentTrip.setCity(city);
            currentTrip.setStartDate(startDate);
            currentTrip.setEndDate(endDate);
            tripId = dbHelper.updateTrip(currentTrip);
        }

        if (tripId != -1) {
            Trip tripForNotification;
            if (currentTrip == null) {
                tripForNotification = new Trip(city, startDate, endDate);
                tripForNotification.setId(tripId);
            } else {
                tripForNotification = currentTrip;
            }
            scheduleNotification(tripForNotification);

            Toast.makeText(this, "Voyage créé/modifié avec succès !", Toast.LENGTH_SHORT).show();
            // Utilisation des Intents pour la navigation
            Intent intent = new Intent(AddEditTripActivity.this, ListPlaceActivity.class);
            intent.putExtra("TRIP_ID", tripId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Erreur lors de la création", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleNotification(Trip trip) {
        // 1. Récupérer la préférence (7 jours ou 2 jours)
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String noticePref = prefs.getString("NOTICE_DELAY", "7"); // "7" par défaut
        int daysBefore = Integer.parseInt(noticePref);

        // 2. Convertir la date de début du voyage (String) en millisecondes
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = sdf.parse(trip.getStartDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            // Soustraire les jours (ex: -7 jours)
            calendar.add(Calendar.DAY_OF_YEAR, -daysBefore);

            // Optionnel : Régler une heure précise (ex: 09:00 du matin)
            // pour ne pas recevoir de notif à minuit pile.
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);

            // 3. Programmer l'alarme
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("CITY", trip.getCity());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) trip.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis())  {
                // SI LA DATE EST PASSÉE OU C'EST AUJOURD'HUI :
                // On peut décider de l'envoyer dans 10 secondes pour le test
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
                Toast.makeText(this, "Notification prévue dans 10s (délai court)", Toast.LENGTH_SHORT).show();
            } else {
                // SINON : On déclenche à la date calculée
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}