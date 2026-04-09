package com.example.trip_organizer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
    MediaPlayer player;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player == null) {
            player = MediaPlayer.create(this, R.raw.towards_the_clouds);
            player.setLooping(true);
        }
            // On ne lance start() QUE si ce n'est pas déjà en train de jouer
        if (!player.isPlaying()) {
            player.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}