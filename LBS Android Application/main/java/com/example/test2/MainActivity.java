package com.example.test2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView myanime;
    //Codes we want to run in Override
    @Override
    //start app after user Entered
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //contain all of my screen
        EdgeToEdge.enable(this);
        //sourese xml (UI)
        setContentView(R.layout.activity_main);

        myanime = findViewById(R.id.textViewpower);
        // define animation
        // this: reference to this page(MainActivity)
        Animation mymovetest = AnimationUtils.loadAnimation(this,R.anim.textmove);
        myanime.startAnimation(mymovetest);

        // move from MainActivity to work_activity
        new Handler().postDelayed(new Runnable  (){
            @Override
            public void run () {
                Intent movetomap = new Intent(MainActivity.this, work_activity.class);
                startActivity(movetomap);
                finish();
            }

        },3600);

        //for lock in PORTRAIT mode _ dont rotate
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}