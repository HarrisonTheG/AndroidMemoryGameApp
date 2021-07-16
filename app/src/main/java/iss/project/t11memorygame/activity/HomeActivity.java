package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wajahatkarim3.easyflipview.EasyFlipView;

import iss.project.t11memorygame.R;
import iss.project.t11memorygame.service.BGMusicService;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    //services reference
    private BGMusicService bgMusicService;

    //UI element reference
    private EasyFlipView musicImage;
    private TextView matchView;
    private TextView scoreView;

    //custom variables
    private boolean IS_MUSIC_ON = true;
    private int totalMatch;
    private long bestScore;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        setUIElements();

        loadMatchAndScore();

        //making sure sound icon display correctly. Music will always be on when starting
        if (IS_MUSIC_ON && bgMusicService == null) {
            if(musicImage.isBackSide())
                musicImage.flipTheView();
            Intent music = new Intent(this, BGMusicService.class);
            bindService(music, this, BIND_AUTO_CREATE);
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        //navigation to browse image page
        if(id == R.id.play){
            Intent intent = new Intent(this, SearchImageActivity.class);
            intent.putExtra("isMusicOn", IS_MUSIC_ON);
            startActivity(intent);
        }
        //background music toggle
        else if (id == R.id.soundToggle){
            musicImage.flipTheView();
            checkMusic();
        }

    }

    //register and connect UI elements on Home Screen
    protected void setUIElements(){
        //register play button
        Button playButton = (Button) findViewById(R.id.play);
        playButton.setOnClickListener(this);

        //register the sound icon
        musicImage = (EasyFlipView) findViewById(R.id.soundToggle);
        musicImage.setOnClickListener(this);

        //register textViews
        matchView = (TextView) findViewById(R.id.totalGames);
        scoreView = (TextView) findViewById(R.id.bestScore);
    }

    //load sharedPreference data on number of matches and best score
    protected void loadMatchAndScore(){

        pref = getSharedPreferences("userData", Context.MODE_PRIVATE);

        totalMatch = pref.getInt("totalMatch", 0);
        bestScore = pref.getLong("bestScore", 0);

        setMatchAndScore();
    }

    //set number of matches and best score
    private void setMatchAndScore(){
        if(bestScore == 0)
            scoreView.setText("-");
        else{
            scoreView.setText(timeToString(bestScore));
        }
        matchView.setText(String.valueOf(totalMatch));

    }

    //convert long time to String
    private String timeToString (long time){
        int hours = (int) (time / 3600000);
        int minutes = (int) (time - hours * 3600000) / 60000;
        int seconds = (int) (time - hours * 3600000 - minutes * 60000) / 1000;
        String duration = minutes + " mins " + seconds + " secs";
        if(hours == 0)
            return duration;
        return hours + " hours " + duration;
    }

    //custom music checking class
    protected void checkMusic(){
        if(!IS_MUSIC_ON){
            Intent music = new Intent(this, BGMusicService.class);
            bindService(music, this, BIND_AUTO_CREATE);
            IS_MUSIC_ON = true;
            Toast.makeText(getApplicationContext(),"Music unmuted",Toast.LENGTH_SHORT).show();
        }else{
            IS_MUSIC_ON = false;
            bgMusicService.mute();
            unbindService(this);
            Toast.makeText(getApplicationContext(),"Music muted",Toast.LENGTH_SHORT).show();
        }
    }

    //Background music lifecycle and binding
    //life cycles
    @Override
    public void onPause(){
        super.onPause();
        if(bgMusicService!=null)
            bgMusicService.pause();
    }

    @Override
    public void onResume(){
        super.onResume();
        // restore
        if(bgMusicService!=null)
            bgMusicService.resume();
        else if(IS_MUSIC_ON) {
            Intent music = new Intent(this, BGMusicService.class);
            bindService(music, this, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(bgMusicService!=null)
            unbindService(this);
    }

    //background music connection to bgServiceClass
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        BGMusicService.LocalBinder musicBinder = (BGMusicService.LocalBinder) binder;
        if(binder != null) {
            bgMusicService = musicBinder.getService();
            bgMusicService.playMusic("GAME");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}