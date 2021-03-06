package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import android.media.AudioManager;

import android.media.AudioAttributes;

import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import android.provider.MediaStore;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.wajahatkarim3.easyflipview.EasyFlipView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import iss.project.t11memorygame.Adapter.GameImageAdapter;
import iss.project.t11memorygame.R;
import iss.project.t11memorygame.model.Image;
import iss.project.t11memorygame.service.BGMusicService;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    ImageView curView = null;
    private SoundPool soundPool;
    private int countPair = 0;
    private EasyFlipView flipImage;
    private int numberofAttemps=0;

    private SoundPool sp;
    private HashMap<Integer,Integer> soundMap=new HashMap<>();

    private int prevMatchCount;
    private long prevBestScore;
    private Boolean IS_MUSIC_ON;
    private BGMusicService bgMusicService;
    SharedPreferences pref;

    PopupWindow popupWindow;
    View popupView;
    private long elapsedMillis;
    Chronometer chronometer;

    //setup the images so that there is 2 with the same number
    Integer[] pos = {0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5};

    int currentPosition = -1;
    ArrayList<Bitmap> bitmapImages = new ArrayList<Bitmap>();
    Bitmap[] drawable=new Bitmap[12];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Get the arraylist Integer of Images chosen before loading the bitmap
        Intent intent = getIntent();
        ArrayList<Integer> images = intent.getIntegerArrayListExtra("chosenImages");

        //get from home activity whether music is on
        IS_MUSIC_ON = intent.getBooleanExtra("isMusicOn", false);
        bindMusicService(IS_MUSIC_ON);


        sp = new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
        soundMap.put(1,sp.load(this,R.raw.match,1));
        soundMap.put(2,sp.load(this,R.raw.mismatch,1));


        //SoundPool for click sound-effect
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        int clickSound = soundPool.load(this,R.raw.sound1,1);

        //count-up timer
        chronometer=findViewById(R.id.timer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        //load bitmap images from searchImageActivity
        loadBitmapImages(images);

        //shuffle the images based on the position
        List<Integer> intList = Arrays.asList(pos);
        Collections.shuffle(intList);
        intList.toArray(pos);
        for (Integer i : pos) {
            System.out.println(i);
        }

        //initialise the gridview images
        GridView gridView = (GridView) findViewById(R.id.gridView);
        GameImageAdapter imageAdapter = new GameImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        //Set onclicklistener for each button
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                soundPool.play(clickSound,1,1,1,0,1);
                //for testing purpose: if you want to show popup before game ends:
                //onButtonShowPopupWindowClick(view);

                //first click
                if (currentPosition < 0) {
                    currentPosition = position;
                    curView = (ImageView) view;

                    ((ImageView) view).setImageBitmap(drawable[pos[position]]);

                }
                //1 image already shown
                else {
                    //if reclick the same image- will hide
                    if (currentPosition == position) {
                        ((ImageView) view).setImageResource(R.drawable.logo);

                    }
                    //if you click different image -tohide
                    else if (pos[currentPosition] != pos[position]) {
                        //add mismatch sound effect
                        sp.play(2,1,1,1,0,1);
                        ((ImageView)view).setImageBitmap(drawable[pos[position]]);
                        gridView.setEnabled(false);


                        Handler handler=new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                curView.setImageResource(R.drawable.logo);
                                ((ImageView)view).setImageResource(R.drawable.logo);
                                gridView.setEnabled(true);
                            }
                        },750);

                    }
                    //if you click the correct image
                    else {

                        //add match sound effect
                        sp.play(1,1,1,1,0,1);
                        ((ImageView) view).setImageBitmap(drawable[pos[position]]);
                        TextView matchestext = findViewById(R.id.matches);
                        ++countPair;
                        matchestext.setText(countPair + "of 6 matches");

                        //disable the onclick when its matched
                        curView.setOnClickListener(null);
                        view.setOnClickListener(null);

                        //if all matched- show popup button
                        if (countPair == 6) {


                            //stop timer and save match result
                            elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
//                            String endtime = Long.toString(elapsedMillis);
                            saveUserData(elapsedMillis);
                            chronometer.stop();

                            //show popup box when you win
                            onButtonShowPopupWindowClick(view, elapsedMillis);
                        }
                    }
                    //Calculate number of attempts
                    ++numberofAttemps;
                    currentPosition = -1;
                    TextView attempstext=findViewById(R.id.attempts);
                    attempstext.setText("Total Attempts: "+numberofAttemps);

                }
            }
        });
        setupBtns();

        //Quit game button
        quitBtn();
    }

    //load bitmap images
    public void loadBitmapImages(ArrayList<Integer> intImage){

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        for(int i=0; i<intImage.size(); i++) {
            File destFile = new File(dir, intImage.get(i) +".jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(destFile.getAbsolutePath());
            bitmapImages.add(bitmap);
        }

        //duplicate the bitmapImages to 6 pairs before shuffling
        for(int i=0;i<6;i++){
            drawable[i] = bitmapImages.get(i);
        }
        for(int j=6;j<12;j++){
            drawable[j] = bitmapImages.get(j-6);
        }
    }

    public void onButtonShowPopupWindowClick(View view, Long endtime) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false; //tapping outside does not close the popout
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        //add timing achieved
        String endtime_string = timeToString(endtime);
        TextView popupText = popupWindow.getContentView().findViewById(R.id.endtime);
        if (popupText != null) {
            popupText.setText(endtime_string);
        }

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
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

    protected void setupBtns() {
        int[] ids = {
                R.id.restart,
                R.id.newgame
        };

        for (int i = 0; i < ids.length; i++) {
            Button btn = findViewById(ids[i]);
            if (btn != null) {
                btn.setOnClickListener(this);
            }
        }
    }

    //OnClick event handler for pop-up window
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.restart) {
            popupWindow.dismiss();
            finish();
            startActivity(getIntent());
        }
        else if (id == R.id.newgame) {
            popupWindow.dismiss();
            Intent intent = new Intent(this, HomeActivity.class);

            startActivity(intent);
            finish();
        }
    }

    //Add quit button
    protected void quitBtn() {
        Intent quitIntent = new Intent(this, HomeActivity.class);

        Button quit = findViewById(R.id.quit);
        if (quit != null) {
            quit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(quitIntent);
                    finish();
                }
            });
        }
    }

    //update user Data on total Matches and bestScore based on time elapse
    protected void saveUserData(long duration){
        getUserData();
        SharedPreferences.Editor editor = pref.edit();

        //update best score if duration is shorter than previous score or first time playing
        if((prevBestScore != 0 && prevBestScore > duration) || prevBestScore == 0){
            editor.putLong("bestScore", duration);
        }
        editor.putInt("totalMatch", prevMatchCount + 1);
        editor.commit();
    }

    //get previous user saved data on shared preferences
    protected void getUserData (){
        pref = getSharedPreferences("userData", Context.MODE_PRIVATE);
        prevMatchCount = pref.getInt("totalMatch", 0);
        prevBestScore = pref.getLong("bestScore", 0);
    }

    @Override
    public void onBackPressed() {
        return;
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
            bgMusicService.resume();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    //bind music at beginning
    protected void bindMusicService(boolean isMusicOn) {
        if (isMusicOn) {
            Intent music = new Intent(this, BGMusicService.class);
            bindService(music, this, BIND_AUTO_CREATE);
        }
    }


}