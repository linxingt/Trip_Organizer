package com.example.trip_organizer;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TripAdapter extends ArrayAdapter<Trip> {
    public TripAdapter(Context context, int resource, ArrayList<Trip> trips)
    {
        super(context, resource, trips);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_trip, parent, false);
        }
        TextView city =view.findViewById(R.id.trip_item_city);
        TextView start = view.findViewById(R.id.trip_start);
        TextView end = view.findViewById(R.id.trip_end);

        Trip trip = getItem(position);

        city.setText(trip.getCity());
        start.setText(trip.getStartDate());
        end.setText(trip.getEndDate());

        return view;
    }
}