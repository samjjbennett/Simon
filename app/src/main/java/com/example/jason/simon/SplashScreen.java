package com.example.jason.simon;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

    import android.app.Activity;
    import android.content.Intent;
    import android.os.Bundle;
    import android.os.Handler;
import android.widget.TextView;

public class SplashScreen extends Activity {

        // Splash screen timer
        private static int SPLASH_TIME_OUT = 8000;
        ImageView myAnimation;
        AnimationDrawable myAnimationDrawable;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash);
           // final TextView tv = (TextView)findViewById(R.id.simontx);
            myAnimation = (ImageView) findViewById(R.id.myanimation);
            myAnimationDrawable
                    = (AnimationDrawable) myAnimation.getDrawable();
            myAnimation.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            myAnimationDrawable.start();
                        }
                    }
            );

            new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */


                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(i);

                    // close this activity
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }

    }