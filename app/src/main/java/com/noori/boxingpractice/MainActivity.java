package com.noori.boxingpractice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//ToDo - make multi_screen ,add user to user basic combat skills.

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Timer timer;
    private Handler handler = new Handler();
    Random random = new Random();

    public ProgressBar progressBar;

    public FrameLayout upperLayout;
    public ImageView gloves, correctIndicator, avatar , bag;
    public Button buttonRight, buttonUp, buttonLeft , tap , sound , how_to_play;
    public TextView textLabel;

    /*desc - avatar_showPunch_duration is for evaluating how long we want to show punch directed by the avatar
     isCorrectButtonClicked - 0 for no response , 1 for correct and 2 for incorrect.*/
    public int time , isCorrectButtonClicked, score , progressbarStatus , rand , HighScore , avatar_showPunch_duration;


    /*anyButtonClicked - to know that the user has pressed any key and based on that evaluate if it was in time frame or not,
     correct Button clicked or not if it was in time frame*/
    public boolean anyButtonClicked , isMuted , isHelpButtonClicked;

    String HS,SC;

    public float glovesX;
    public ArrayList<Integer> arrayList = new ArrayList<>();

    public Animation mAnimation , fallAnimation;
    public AnimationDrawable frameAnimation1 , frameAnimation;
    public MediaPlayer ring , ring1;
    public SharedPreferences sharedPreferences;


    //ignore deprecated of getDrawable since my theme is null so no need to use different code for below 21 and after 21 API
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upperLayout =  findViewById(R.id.upper_Layout);

        gloves =  findViewById(R.id.gloves);
        correctIndicator =  findViewById(R.id.correctIndicator);
        avatar =  findViewById(R.id.avatar);
        bag = findViewById(R.id.punchingBag);

        buttonUp =  findViewById(R.id.buttonUp);
        buttonRight =  findViewById(R.id.buttonRight);
        buttonLeft =  findViewById(R.id.buttonLeft);
        sound = findViewById(R.id.sound);
        how_to_play  = findViewById(R.id.how_to_play);
        tap =  findViewById(R.id.TapToStart);

        progressBar =  findViewById(R.id.progress_horizontal);
        textLabel = findViewById(R.id.score);

        tap.setVisibility(View.INVISIBLE);
        anyButtonClicked=false;

        //add in ArrayList to get easy access
        arrayList.add(R.drawable.up);
        arrayList.add(R.drawable.left);
        arrayList.add(R.drawable.right);

        rand = random.nextInt(3);
        gloves.setBackgroundResource(arrayList.get(rand));
        glovesX = gloves.getX();
        progressbarStatus = 100;

        //Initialize all the animations and sounds needed
        avatar.setImageDrawable(getResources().getDrawable(R.drawable.idle_pos));
        frameAnimation = (AnimationDrawable) avatar.getDrawable();
        frameAnimation.start();

        ring = MediaPlayer.create(MainActivity.this,R.raw.punch);
        ring1 = MediaPlayer.create(MainActivity.this,R.raw.wrong1);

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.punch_animation);
        mAnimation.setInterpolator(new LinearInterpolator());
        fallAnimation = AnimationUtils.loadAnimation(this, R.anim.fall_animation);
        fallAnimation.setInterpolator(new LinearInterpolator());

        buttonUp.setOnClickListener(this);
        buttonRight.setOnClickListener(this);
        buttonLeft.setOnClickListener(this);

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isMuted = !isMuted;

                if(isMuted)
                    sound.setText(R.string.sound);
                else
                    sound.setText(R.string.mute);
            }
        });

        how_to_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.how_to_play)
                        .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isHelpButtonClicked = true;
                                onResume();
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        //use timer to create looping of gloves
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dropItem();
                    }
                });
            }
        }, 0, 30);
    }

    public void dropItem(){
        //To show which punch is directed for particular time
        if(avatar_showPunch_duration > 0)
            avatar_showPunch_duration ++;

        //again set the moving animation of avatar
        if(!frameAnimation.isRunning() && avatar_showPunch_duration == 6)
        {
            avatar.setImageDrawable(getResources().getDrawable(R.drawable.idle_pos));
            frameAnimation = (AnimationDrawable) avatar.getDrawable();
            frameAnimation.start();
            avatar_showPunch_duration = 0;
        }

        time += 1;
        glovesX -= 20;

        //when gloves reached the end
        if(glovesX <= 0) {
            progressBar.setProgress(progressbarStatus);
            if(progressbarStatus <= 0)
            {
                gameOver();
            }
            rand = random.nextInt(3);
            gloves.setBackgroundResource(arrayList.get(rand));
            glovesX = upperLayout.getWidth();
            progressbarStatus -= 1;
            time=0;
            anyButtonClicked = false;
        }

        //set updated x ,y coordinate to get moving object simulation
        gloves.setX(glovesX);
        gloves.setY(80.0f);

        //when the gloves is not inside the frame and user clicked the button
        if(!(time >= 20 && time < 42) && anyButtonClicked)
        {
            correctIndicator.setImageDrawable(getResources().getDrawable(R.drawable.wrong_anim));
            frameAnimation1 = (AnimationDrawable) correctIndicator.getDrawable();
            frameAnimation1.start();
            if(!isMuted && !ring1.isPlaying())
                ring1.start();
            progressbarStatus -= 25;
            anyButtonClicked = false;
        }

        //when gloves is inside the frame
        if(time >= 20 && time < 42 && anyButtonClicked )
        {
            if(isCorrectButtonClicked ==1)
            {
                if(progressbarStatus < 100)
                    progressbarStatus += 5;
                changeColor();
                score ++;
                SC = getString(R.string.score) + String.valueOf(score);
                textLabel.setText(SC);
            }
            else if(isCorrectButtonClicked == 2)
            {
                correctIndicator.setImageDrawable(getResources().getDrawable(R.drawable.wrong_anim));
                frameAnimation1 = (AnimationDrawable) correctIndicator.getDrawable();
                frameAnimation1.start();
                progressbarStatus -= 25;
                if(!isMuted)
                    ring1.start();
            }
            anyButtonClicked = false;
        }
    }

    @Override
    public void onClick(View v)
    {
        bag.startAnimation(mAnimation);

        if(!isMuted)
            ring.start();

        anyButtonClicked = true;

        switch (v.getId()){

            case R.id.buttonUp:
                if(rand == 0)
                    isCorrectButtonClicked = 1;
                else
                    isCorrectButtonClicked = 2;

                avatar.setImageDrawable(getResources().getDrawable(R.drawable.punch_up));
                break;

            case R.id.buttonLeft:
                if(rand == 1)
                    isCorrectButtonClicked = 1;
                else
                    isCorrectButtonClicked = 2;

                avatar.setImageDrawable(getResources().getDrawable(R.drawable.punch_left));
                break;

            case R.id.buttonRight:
                if(rand == 2)
                    isCorrectButtonClicked = 1;
                else
                    isCorrectButtonClicked = 2;

                avatar.setImageDrawable(getResources().getDrawable(R.drawable.punch_right));
                break;
        }
        avatar_showPunch_duration = 1;
    }

    public void gameOver(){
        avatar.setImageResource(R.drawable.hit);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.bag_hit_animation);
        mAnimation.setInterpolator(new LinearInterpolator());
        bag.startAnimation(mAnimation);
        avatar.startAnimation(fallAnimation);

        sharedPreferences = getSharedPreferences("HIGH_SCORE", Context.MODE_PRIVATE);
        HighScore = sharedPreferences.getInt("HIGH_SCORE", 0);

        if (score > HighScore) {
            HighScore = score;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("HIGH_SCORE", HighScore);
            editor.apply();
        }
        HS = getString(R.string.hs) + String.valueOf(HighScore);
        textLabel.setText(HS);

        tap.setVisibility(View.VISIBLE);
        buttonLeft.setVisibility(View.INVISIBLE);
        buttonRight.setVisibility(View.INVISIBLE);
        buttonUp.setVisibility(View.INVISIBLE);

        timer.cancel();
        timer = null;

        tap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public  void changeColor(){
        if(rand == 0)
            gloves.setBackgroundResource(R.drawable.correctup);
        else if(rand == 1)
            gloves.setBackgroundResource(R.drawable.correctleft);
        else
            gloves.setBackgroundResource(R.drawable.correctright);

        isCorrectButtonClicked = 0;
    }

    //cancel the timer and saves the state of variables
    @Override
    protected void onPause() {
        if(timer!=null)
            timer.cancel();

        isHelpButtonClicked = true;
        super.onPause();
    }

    //create new timer and starts the looping of gloves ;
    // since every field was saved therefore every states start from where it was left.
    @Override
    protected void onResume() {
        //checks if the user came after pausing state , after reading help section and not during creation of activity
        if(isHelpButtonClicked)
        {
            isHelpButtonClicked = false;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dropItem();
                        }
                    });

                }
            }, 0, 30);
        }
        super.onResume();
    }
}
