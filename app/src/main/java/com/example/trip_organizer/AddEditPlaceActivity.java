package com.example.trip_organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditPlaceActivity extends AppCompatActivity {
    private EditText etTitle, etDesc, etAddress, etPhone, etDate, etHour;
    private Button btnSave, btnMaps, btnCall, btnPickDate, btnPickHour, btnVisited, btnShare, btnPhoto;
    private DatabaseHelper dbHelper;
    private long tripId;
    private int isVisited = 0; // 0 = non, 1 = oui
    private Place placeToEdit;
    private ImageView ivPhoto;
    private String mode = "ADD"; // Par défaut
    private String selectedImageUri = "";
    private final ActivityResultLauncher<String[]> mGetContent = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);

                        File file = new File(getFilesDir(), "img_" + System.currentTimeMillis() + ".jpg");
                        OutputStream outputStream = new FileOutputStream(file);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }

                        outputStream.close();
                        inputStream.close();

                        selectedImageUri = file.getAbsolutePath();
                        ivPhoto.setImageURI(Uri.fromFile(file));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_place);

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.open();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Initialisation des vues
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.etm_desc);
        etAddress = findViewById(R.id.etm_adress);
        etPhone = findViewById(R.id.et_phone);
        etDate = findViewById(R.id.et_date_place);
        etHour = findViewById(R.id.et_hour_place);
        ivPhoto = findViewById(R.id.iv_place_photo);
        btnSave = findViewById(R.id.btn_save_place);
        btnMaps = findViewById(R.id.btn_map);
        btnCall = findViewById(R.id.btn_dial);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickHour = findViewById(R.id.btn_pick_time);
        btnVisited = findViewById(R.id.btn_visited);
        btnShare = findViewById(R.id.btn_share);
        btnPhoto = findViewById(R.id.btn_photo);

        // Récupération des données de l'Intent
        placeToEdit = getIntent().getParcelableExtra("PLACE_OBJECT");
        String intentMode = getIntent().getStringExtra("MODE");
        if (intentMode != null) mode = intentMode;
        tripId = getIntent().getLongExtra("TRIP_ID", -1);

        configureMode();

        btnSave.setOnClickListener(v -> {
            savePlace();
        });
        btnPhoto.setOnClickListener(v -> mGetContent.launch(new String[]{"image/*"}));
        btnVisited.setOnClickListener(v -> {
            isVisited = (isVisited == 0) ? 1 : 0;
            placeToEdit.setIsVisited(isVisited);
            dbHelper.updatePlace(placeToEdit);
            updateVisitedButtonText();
        });
        btnMaps.setOnClickListener(this::openMaps);
        btnCall.setOnClickListener(this::makeCall);
        btnPickDate.setOnClickListener(this::showDatePicker);
        btnPickHour.setOnClickListener(this::showTimePicker);
        btnShare.setOnClickListener(this::sharePlace);
    }

    private void updateVisitedButtonText() {
        btnVisited.setText(isVisited == 1 ? R.string.btn_place_not_visited : R.string.btn_place_visited);
    }

    private void configureMode() {
        if (mode.equals("DETAIL")) {
            // MODE DETAIL : Cacher les outils d'édition, montrer les actions
            btnSave.setVisibility(View.GONE);
            btnPhoto.setVisibility(View.GONE);
            btnPickDate.setVisibility(View.GONE);
            btnPickHour.setVisibility(View.GONE);

            // Verrouiller les champs texte
            etTitle.setEnabled(false);
            etDesc.setEnabled(false);
            etAddress.setEnabled(false);
            etPhone.setEnabled(false);
            etDate.setEnabled(false);
            etHour.setEnabled(false);

            // Montrer les boutons d'action
            btnMaps.setVisibility(View.VISIBLE);
            btnCall.setVisibility(placeToEdit.getPhone().isEmpty() ? View.GONE : View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
            btnVisited.setVisibility(View.VISIBLE);

        } else if (mode.equals("EDIT") || mode.equals("ADD")) {
            // MODE AJOUT ou EDITION : Cacher les boutons d'action
            btnMaps.setVisibility(View.GONE);
            btnCall.setVisibility(View.GONE);
            btnShare.setVisibility(View.GONE);
            btnVisited.setVisibility(View.GONE);

            // Montrer les outils de saisie
            btnSave.setVisibility(View.VISIBLE);
            btnPhoto.setVisibility(View.VISIBLE);
            btnPickDate.setVisibility(View.VISIBLE);
            btnPickHour.setVisibility(View.VISIBLE);

            etTitle.setEnabled(true);
            etDesc.setEnabled(true);
            etAddress.setEnabled(true);
            etPhone.setEnabled(true);
            etDate.setEnabled(true);
            etHour.setEnabled(true);

            if (mode.equals("EDIT")) {
                btnSave.setText(R.string.menu_edit); // Affiche "Edit" au lieu de "Save"
            } else btnSave.setText(R.string.btn_save);
        }
        // Remplir avec les données de placeToEdit
        if (placeToEdit != null) {
            etTitle.setText(placeToEdit.getTitle());
            etAddress.setText(placeToEdit.getAddress());
            etDate.setText(placeToEdit.getDate());
            etHour.setText(placeToEdit.getHour());
            etDesc.setText(placeToEdit.getDescription());
            etPhone.setText(placeToEdit.getPhone());
            isVisited = placeToEdit.getIsVisited();
            updateVisitedButtonText();
            if (placeToEdit.getPhoto() != null && !placeToEdit.getPhoto().isEmpty()) {
                try {
                    ivPhoto.setImageURI(Uri.parse(placeToEdit.getPhoto()));
                } catch (Exception e) {
                    ivPhoto.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                // Si le chemin est null ou vide ""
                ivPhoto.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    // --- Gestion des Pickers ---
    public void showDatePicker(View v) {
        Calendar c = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", c.get(Calendar.YEAR));
        args.putInt("month", c.get(Calendar.MONTH));
        args.putInt("day", c.get(Calendar.DAY_OF_MONTH));
        DatePickerFragment dateFrag = new DatePickerFragment();
        dateFrag.setArguments(args);
        dateFrag.setCallBack((view, year, month, dayOfMonth) ->
                etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year));
        dateFrag.show(getSupportFragmentManager(), "Date Picker");
    }

    public void showTimePicker(View v) {
        Calendar c = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("hours", c.get(Calendar.HOUR_OF_DAY));
        args.putInt("minutes", c.get(Calendar.MINUTE));
        TimePickerFragment timeFrag = new TimePickerFragment();
        timeFrag.setArguments(args);
        timeFrag.setCallBack((view, hourOfDay, minute) ->
                etHour.setText(hourOfDay + ":" + minute));
        timeFrag.show(getSupportFragmentManager(), "Time Picker");
    }

    // --- Actions Spéciales ---
    public void openMaps(View v) {
        // 1. Récupérer l'adresse et l'encoder pour l'URL (important pour les espaces/caractères spéciaux)
        String address = etAddress.getText().toString();
        // 2. Utiliser le format geo:0,0?q= (0,0 signifie qu'on cherche par texte et non par coordonnées précises)
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        // 3. Créer l'Intent
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Message d'erreur si Maps n'est pas installé
            Toast.makeText(this, "Google Maps n'est pas installé", Toast.LENGTH_SHORT).show();
        }
    }

    public void makeCall(View v) {
        String phone = etPhone.getText().toString();
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void savePlace() {
        String title = etTitle.getText().toString();
        String address = etAddress.getText().toString();
        String desc = etDesc.getText().toString();
        String date = etDate.getText().toString().trim();
        String hour = etHour.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (title.isEmpty() || address.isEmpty() || desc.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Titre, Description, Date et Adresse obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dateLieu = sdf.parse(date);

            // récupérer les dates du voyage parent (via la DB)
            Trip parentTrip = dbHelper.getTripById(tripId);
            Date debutVoyage = sdf.parse(parentTrip.getStartDate());
            Date finVoyage = sdf.parse(parentTrip.getEndDate());

            if (dateLieu.before(debutVoyage) || dateLieu.after(finVoyage)) {
                Toast.makeText(this, "La date doit être comprise entre le " + parentTrip.getStartDate() + " et le " + parentTrip.getEndDate(), Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!phone.isEmpty()) {
            String phoneRegex = "^[0-9]{10}$";
            if (!phone.matches(phoneRegex)) {
                Toast.makeText(this, "Format téléphone invalide (10 chiffres attendus)", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (placeToEdit == null) {
            // Mode AJOUT
            Place p = new Place(tripId, title, desc, date, hour, address, phone, selectedImageUri, isVisited);
            dbHelper.addPlace(p);
        } else {
            String photo = selectedImageUri==""?placeToEdit.getPhoto():selectedImageUri;
            // Mode MODIFICATION
            placeToEdit.setTitle(title);
            placeToEdit.setAddress(address);
            placeToEdit.setDate(date);
            placeToEdit.setHour(hour);
            placeToEdit.setDescription(desc);
            placeToEdit.setPhone(phone);
            placeToEdit.setPhoto(photo);
            placeToEdit.setIsVisited(isVisited);
            dbHelper.updatePlace(placeToEdit);
        }
        finish(); // Retour à la liste
    }

    public void sharePlace(View v) {
        String shareBody = getString(R.string.hint_place_title) + " : " + etTitle.getText().toString() +
                "\n" + getString(R.string.hint_place_address) + " : " + etAddress.getText().toString() +
                "\n" + getString(R.string.label_date) + " : " + etDate.getText().toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check ce lieu !");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Partager via"));
    }
}