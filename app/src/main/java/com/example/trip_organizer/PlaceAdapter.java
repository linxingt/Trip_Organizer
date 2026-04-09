package com.example.trip_organizer;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlaceAdapter extends ArrayAdapter<Place> {
    public PlaceAdapter(Context context, int resource, ArrayList<Place> places)
    {
        super(context, resource, places);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_place, parent, false);
        }
        TextView title =view.findViewById(R.id.place_item_title);
        TextView date = view.findViewById(R.id.place_item_date);
        TextView visited = view.findViewById(R.id.place_item_visted);
        ImageView img = view.findViewById(R.id.place_photo);

        Place place = getItem(position);

        title.setText(place.getTitle());
        date.setText(place.getDate());
        // --- Gestion du symbole Visité (Vert/Rouge) ---
        if (place.getIsVisited() == 1) {
            visited.setText("✅");
//            visited.setTextColor(android.graphics.Color.GREEN);
        } else {
            visited.setText("🔴"); // Rond rouge pour non visité
        }
        // --- Image ---
        File imgFile = new File(place.getPhoto());
        if (imgFile.exists()) {
            img.setImageURI(Uri.fromFile(imgFile));
        } else {
            img.setImageResource(R.drawable.ic_launcher_foreground);
        }

        return view;
    }
}