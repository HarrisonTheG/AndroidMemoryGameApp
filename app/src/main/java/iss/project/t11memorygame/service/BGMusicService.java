package iss.project.t11memorygame.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import iss.project.t11memorygame.R;

public class BGMusicService extends Service {
    MediaPlayer BGMusicPlayer;
    //IN ACTIVITY:
    //if user choose to mute this attr. (IS_MUTED)become true.
    //Then service becomes unbind also.
    //Boolean IS_MUTED = false ;

    //use binder to connect service and activity
    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
            public BGMusicService getService(){
                return BGMusicService.this;
            }
    }

    @Override
    public IBinder onBind(Intent intent){
        Log.i("MusicLog", "BackgroundMusicService -> onBind, Thread: " + Thread.currentThread().getName());
        return binder;
    }
    @Override
    public boolean onUnbind(Intent intent){
        Log.i("MusicLog", "BackgroundMusicService -> onUnBind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i("MusicLog", "BackgroundMusicService -> onDestroy");
        super.onDestroy();
    }


    public void playMusic(String scene){
        if(BGMusicPlayer != null) {
            BGMusicPlayer.reset();
            //BGMusicPlayer.release();
            //BGMusicPlayer = null;
        }
        switch (scene) {
            case "GAME": {
                BGMusicPlayer = MediaPlayer.create(this, R.raw.ocean);
                Log.i("MusicLog", "Play music GAME");
                break;
            }

        }
        BGMusicPlayer.setLooping(true);
        BGMusicPlayer.start();
    }


    public void pause() {
        if (BGMusicPlayer != null && BGMusicPlayer.isPlaying()) {
            BGMusicPlayer.pause();
        }
    }


    public void resume() {
        if (BGMusicPlayer != null && !BGMusicPlayer.isPlaying()) {
            BGMusicPlayer.start();
        }
    }

    public void mute() {
        if (BGMusicPlayer != null && BGMusicPlayer.isPlaying()) {
            BGMusicPlayer.stop();
            BGMusicPlayer.release();
            BGMusicPlayer = null;
        }
    }

}
