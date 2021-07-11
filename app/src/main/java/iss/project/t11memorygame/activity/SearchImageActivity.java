package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;


import iss.project.t11memorygame.R;
import iss.project.t11memorygame.service.BGMusicService;


public class SearchImageActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection{

    private BGMusicService bgMusicService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_image);

        //get from home activity whether music is on
        Intent intent = getIntent();
        bindMusicService(intent.getBooleanExtra("isMusicOn", false));
    }


    @Override
    public void onClick(View view) {

    }

//-- Background Music Task

    protected void bindMusicService(boolean isMusicOn){
        if(isMusicOn){
            Intent music = new Intent(this, BGMusicService.class);
            bindService(music, this, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        BGMusicService.LocalBinder musicBinder = (BGMusicService.LocalBinder) binder;
        if(binder != null) {
            bgMusicService = musicBinder.getService();
            bgMusicService.resume();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}