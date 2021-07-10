package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import iss.project.t11memorygame.R;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //register play button
        Button playButton = (Button) findViewById(R.id.play);
        playButton.setOnClickListener(this);
    }

    //testing
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.play){
            Intent intent = new Intent(this, SearchImageActivity.class);
            startActivity(intent);
        }
    }

}