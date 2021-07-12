package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import iss.project.t11memorygame.Adapter.GameImageAdapter;
import iss.project.t11memorygame.R;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView curView = null;
    private SoundPool soundPool;
    private int countPair = 0;
    private EasyFlipView flipImage;
    private int numberofAttemps=0;
    private int playerOneScore=0;
    private int playerTwoScore=0;
    private boolean playerOneTurn=true;



//    Chronometer chronometer;
//    private long time;

    //dummy images, can remove
//    final int[] drawable=new int[]{
//            R.drawable.r15,
//            R.drawable.r3,
//            R.drawable.r1,
//            R.drawable.monster,
//            R.drawable.v2,
//            R.drawable.s1000rr
//    };

    //setup the images so that there is 2 with the same number
    Integer[] pos = {0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5};

    int currentPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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

        //Count-Up timer
        Chronometer timer = (Chronometer) findViewById(R.id.timer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();


//        TextView tv=(TextView)findViewById(R.id.timer) ;
//        new CountDownTimer(120*1000,1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                tv.setText(" You left "+String.format("0%d : %d ",
//                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
//                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)-
//                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
//            }
//            @Override
//            public void onFinish() {
//                tv.setText("Time is out");
//            }
//        }.start();



////        countup timer
//        chronometer=findViewById(R.id.timer);
//        chronometer.setBase(SystemClock.elapsedRealtime());
//        time=SystemClock.elapsedRealtime();
//        chronometer.start();



        //get the images from the SearchImageActivity
        Intent intent = getIntent();
        ArrayList<Integer> chosenimages = intent.getIntegerArrayListExtra("images");
        int[] drawable = chosenimages.stream().mapToInt(i -> i).toArray();

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

        if(playerOneTurn){
            TextView p1score=findViewById(R.id.p1score);
            p1score.setTypeface(Typeface.DEFAULT_BOLD);
        }


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

//                    flipImage=(EasyFlipView) findViewById(R.id.grid_image);
//                    ImageView frontview=findViewById(R.id.front);
//                    frontview.setImageResource(drawable[pos[position]]);
//                    ImageView backview=findViewById(R.id.back);
//                    backview.setImageResource(R.drawable.logo);
//                    flipImage.flipTheView();

                    ((ImageView) view).setImageResource(drawable[pos[position]]);

                }
                //1 image already shown
                else {
                    //if reclick the same image- will hide
                    if (currentPosition == position) {
                        ((ImageView) view).setImageResource(R.drawable.logo);

                    }
                    //if you click different image -tohide
                    else if (pos[currentPosition] != pos[position]) {
                        ((ImageView)view).setImageResource(drawable[pos[position]]);
                        Handler handler=new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                curView.setImageResource(R.drawable.logo);
                                ((ImageView)view).setImageResource(R.drawable.logo);
                            }
                        },300);

                    }
                    //if you click the correct image
                    else {

                        ((ImageView) view).setImageResource(drawable[pos[position]]);
                        TextView matchestext = findViewById(R.id.matches);
                        ++countPair;
                        matchestext.setText(countPair + "of 6 matches");


                        //disable the onclick when its matched
                        curView.setOnClickListener(null);
                        view.setOnClickListener(null);

                        if(playerOneTurn)
                            ++playerOneScore;
                        else
                            ++playerTwoScore;

                        //if all matched- show popup button
                        if (countPair == drawable.length) {
                            Toast.makeText(getApplicationContext(), "you win", Toast.LENGTH_SHORT).show();
                            //show popup box when you win
                            onButtonShowPopupWindowClick(view);
                            timer.stop();
                        }
                    }
                    //Calculate nuber of attemps
                    ++numberofAttemps;
                    currentPosition = -1;
                    TextView attempstext=findViewById(R.id.attempts);
                    attempstext.setText("Total Attempts: "+numberofAttemps);

                    //go to p2 and set
                    if(playerOneTurn)
                        playerOneTurn=false;

                    else
                        playerOneTurn=true;

                    TextView p1score=findViewById(R.id.p1score);
                    TextView p2score=findViewById(R.id.p2score);

                    if(playerOneTurn){
                        p1score.setTypeface(Typeface.DEFAULT_BOLD);
                        p2score.setTypeface(Typeface.DEFAULT);
                        p1score.setText("P1: "+p1score.getText().toString());
                    }
                    else{
                        p2score.setTypeface(Typeface.DEFAULT_BOLD);
                        p1score.setTypeface(Typeface.DEFAULT);
                        p2score.setText("P2: "+p2score.getText().toString());
                    }
                }
            }
        });
        setupBtns();
    }

    public void onButtonShowPopupWindowClick(View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false; //tapping outside does not close the popout
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.restart) {
            finish();
            Intent intent = getIntent();
            ArrayList<Integer> chosenimages = intent.getIntegerArrayListExtra("images");
            int[] drawable = chosenimages.stream().mapToInt(i -> i).toArray();

            startActivity(intent);
        }
        else if (id == R.id.newgame) {
            Intent intent = new Intent(this, SearchImageActivity.class);
            startActivity(intent);
            finish();
        }
    }


}