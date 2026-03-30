package com.example.trip_organizer;

import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable {
    private long id;
    private long tripId;
    private String title;
    private String description;
    private String date;
    private String hour;
    private String address;
    private String phone;
    private String photo;
    private int isVisited; // 0 = faux, 1 = vrai pour SQLite

    public Place(long tripId, String title, String description, String date, String address, String hour, String phone, String photo, int isVisited) {
        this.tripId = tripId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.address = address;
        this.hour = hour;
        this.phone = phone;
        this.photo = photo;
        this.isVisited = isVisited;
    }

    public Place(long id, long tripId, String title, String description, String date, String hour, String address, String phone, String photo, int isVisited) {
        this(tripId, title, description, date, hour, address, phone, photo, isVisited);
        this.id = id;
    }

    // --- Logique Parcelable ---
    protected Place(Parcel in) {
        id = in.readLong();
        tripId = in.readLong();
        title = in.readString();
        description = in.readString();
        date = in.readString();
        hour = in.readString();
        address = in.readString();
        phone = in.readString();
        photo = in.readString();
        isVisited = in.readInt();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(tripId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeString(hour);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(photo);
        dest.writeInt(isVisited);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getIsVisited() {
        return isVisited;
    }

    public void setIsVisited(int isVisited) {
        this.isVisited = isVisited;
    }
}