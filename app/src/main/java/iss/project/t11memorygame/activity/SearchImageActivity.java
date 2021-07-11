package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

import iss.project.t11memorygame.Adapter.SearchImageAdapter;
import iss.project.t11memorygame.R;

import iss.project.t11memorygame.service.BGMusicService;

import iss.project.t11memorygame.model.ChosenImage;
public class SearchImageActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    ArrayList<Integer> chosen = new ArrayList<>();

    private BGMusicService bgMusicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_image);


        //get from home activity whether music is on
        Intent intent = getIntent();
        bindMusicService(intent.getBooleanExtra("isMusicOn", false));

        int[] drawables = {
                R.drawable.r15,
                R.drawable.r3,
                R.drawable.r1,
                R.drawable.monster,
                R.drawable.v2,
                R.drawable.s1000rr,
                R.drawable.cbr1000rr,
                R.drawable.gs1200
        };


        //initialise the gridview images
        GridView gridView = (GridView) findViewById(R.id.gridViewImagesToChoose);
        SearchImageAdapter imageAdapter = new SearchImageAdapter(this, drawables);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Tapped", Toast.LENGTH_SHORT).show();
                chosen.add(drawables[position]);
                view.setOnClickListener(null);
                view.setBackgroundColor(Color.GRAY);

                TextView matchestext = findViewById(R.id.matches);
                matchestext.setText(chosen.stream().count() + "Out of 6 images");
                if (chosen.stream().count() == 6) {
                    Toast.makeText(getApplicationContext(), "You have Chosen 6 Images", Toast.LENGTH_SHORT).show();
                    onClick(view);
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        ChosenImage chosenones = new ChosenImage();
        ArrayList<Integer> chosenimages = chosenones.getChoices();
        for (Integer i : chosen) {
            chosenimages.add(i);
        }

        intent.putExtra("images", chosenimages);
        startActivity(intent);
    }


//-- Background Music Task

    protected void bindMusicService(boolean isMusicOn) {
        if (isMusicOn) {
            Intent music = new Intent(this, BGMusicService.class);
            bindService(music, this, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        BGMusicService.LocalBinder musicBinder = (BGMusicService.LocalBinder) binder;
        if (binder != null) {
            bgMusicService = musicBinder.getService();
            bgMusicService.resume();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}