package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import iss.project.t11memorygame.Adapter.GameImageAdapter;
import iss.project.t11memorygame.R;
import iss.project.t11memorygame.model.ChosenImage;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView curView = null;
    private int countPair = 0;

    //dummy images, can remove
//    final int[] drawable=new int[]{
//            R.drawable.r15,
//            R.drawable.r3,
//            R.drawable.r1,
//            R.drawable.monster,
//            R.drawable.v2,
//            R.drawable.s1000rr
//    };
    final int[] restartgamewithsamedrawable=new int[]{};

    //setup the images so that there is 2 with the same number
    Integer[] pos = {0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5};

    int currentPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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

        //Set onclicklistener for each button
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //for testing purpose: if you want to show popup before game ends:
                //onButtonShowPopupWindowClick(view);

                //first click
                if (currentPosition < 0) {
                    currentPosition = position;
                    curView = (ImageView) view;
                    ((ImageView) view).setImageResource(drawable[pos[position]]);
                    Toast.makeText(getApplicationContext(), "First Click: Position is: " + position + "CurrentPosition is: " + currentPosition, Toast.LENGTH_SHORT).show();
                    System.out.println("First Click: Position is: " + position + "CurrentPosition is: " + currentPosition);
                }
                //1 image already shown
                else {
                    //if reclick the same image- will hide
                    if (currentPosition == position) {
                        ((ImageView) view).setImageResource(R.drawable.hidden);
                        Toast.makeText(getApplicationContext(), "This is 2nd tap ", Toast.LENGTH_SHORT).show();
                        System.out.println("you click the same image, current position is: " + currentPosition + "This image position is: " + position);
                    }
                    //if you click different image -tohide
                    else if (pos[currentPosition] != pos[position]) {
                        //((ImageView)view).setImageResource(drawable[pos[position]]);
                        curView.setImageResource(R.drawable.hidden);
                        Toast.makeText(getApplicationContext(), "notmatch", Toast.LENGTH_SHORT).show();
                        System.out.println("Something is oepn, but didnt match, opened is : " + currentPosition + "   what you tapped is " + position);
                    }
                    //if you click the correct image
                    else {
                        Toast.makeText(getApplicationContext(), "You match Curent Position:   " + currentPosition + " with " + pos[position], Toast.LENGTH_SHORT).show();
                        ((ImageView) view).setImageResource(drawable[pos[position]]);
                        TextView matchestext = findViewById(R.id.matches);
                        ++countPair;
                        matchestext.setText(countPair + "of 6 matches");
                        System.out.println("you matched, first image position is " + currentPosition + " what you newly opened is " + position);

                        //disable the onclick when its matched
                        curView.setOnClickListener(null);
                        view.setOnClickListener(null);

                        //if all matched- show popup button
                        if (countPair == drawable.length) {
                            Toast.makeText(getApplicationContext(), "you win", Toast.LENGTH_SHORT).show();
                            //show popup box when you win
                            onButtonShowPopupWindowClick(view);
                        }
                    }
                    currentPosition = -1;
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