package com.example.jason.simon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private Player simon;
    private boolean game_finished=false;
    private UpdateTask updateTask = null;
    private UpdateTask newUpdateTask = null;
    private int round;
    private int playerRound;
    private TextView tv;
    private boolean playersTurn;
    int highScore;

    SoundPool soundPool;
    private Set<Integer> soundsLoaded;
    private int[] bp_sound;

    // Countdown timmer
    private boolean isCounterRunning  = false;
    final int start_time = 6000;
    final int interval = 1000;
    private CountDownTimer mCountDownTimer;
    private int[] soundsId = {R.raw.meow, R.raw.swooshr, R.raw.creakydoor, R.raw.special_razz, R.raw.lose, R.raw.death_is_your_only_escape, R.raw.dammit};

    private int[] imageButtonId ={R.id.red_off, R.id.blue_off, R.id.yellow_off, R.id.green_off};
    private int[] imagesOff ={R.drawable.red_off, R.drawable.blue_off, R.drawable.yellow_off, R.drawable.green_off};
    private int[] imagesOn = {R.drawable.red_on, R.drawable.blue_on, R.drawable.yellow_on, R.drawable.green_on};

    ImageButton[] imgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.round);
        imgBtn = new ImageButton[4];
        for(int i=0; i< 4; i++) {
            imgBtn[i] = (ImageButton) findViewById(imageButtonId[i]);
        }

        for(int i=0; i< 4; i++) {
            findViewById(imageButtonId[i]).setBackgroundResource(imagesOff[i]);

        }
        findViewById(R.id.startBtn).setOnClickListener(new StartButtonListener());
        findViewById(R.id.stopBtn).setOnClickListener(new StopButtonListener());
        findViewById(R.id.resetBtn).setOnClickListener(new StopButtonListener());

        for(int i=0; i< 4;i++){
            findViewById(imageButtonId[i]).setOnClickListener(new GameButtonListener());
        }
        try {
            FileInputStream in = openFileInput("highscore.txt");
            Scanner scanner = new Scanner(in);
            highScore = scanner.nextInt();
            TextView hs = (TextView)findViewById(R.id.highScore);
            hs.setText("High Score:" + Integer.toString(highScore));
            scanner.close();
        } catch (FileNotFoundException e){
            highScore = 0;
        }
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                                                @Override
                                                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                                                    if (status == 0) { // success
                                                        soundsLoaded.add(sampleId);
                                                    } else {
                                                        Log.w("SOUND_POOL", "WARNING: status is " + status + "???????");
                                                    }
                                                }
                                            }
        );
        bp_sound = new int[20];
        soundsLoaded = new HashSet<>();
        // load sounds
        for(int i =0; i< soundsId.length; i++){
            bp_sound[i]= soundPool.load(getApplicationContext(),soundsId[i],0);
        }

        // Countdown timer
       mCountDownTimer = new CountDownTimer(start_time, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv.setText("Count down timer " + millisUntilFinished/interval );
            }

            @Override
            public void onFinish() {
                isCounterRunning = false;
                if (!game_finished) {
                    youLostDialog();
                    playSound(soundsId[6], 0);
                    tv.setText("Out of time!!!");
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class GameButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(playersTurn == true) {
                int choice = -1;
                if (v.getId() == imageButtonId[0]) {
                    choice = 0;
                    setCountDownTimer();
                } else if (v.getId() == imageButtonId[1]) {
                    choice = 1;
                    setCountDownTimer();
                } else if (v.getId() == imageButtonId[2]) {
                    choice = 2;
                    setCountDownTimer();
                } else if (v.getId() == imageButtonId[3]) {
                    choice = 3;
                    setCountDownTimer();

                }
                //Toast.makeText(getApplicationContext(), Integer.toString(choice)+Integer.toString(playerRound), Toast.LENGTH_SHORT).show();
                Log.i("PLAYER", Integer.toString(choice));
                //  player.storeChoice(choice);
                if(simon.getChoice(playerRound-1) != choice){
                    onLose();
                    return;
                }else{
                    playSound(bp_sound[choice],0);
                }
                playerRound++;
                if(playerRound == round){
                    updateTask = new UpdateTask();
                    setButtonClickableStatus(false);
                    updateTask.execute();
                }
            }
        }
    }
    public void onLose(){
        Toast.makeText(getApplicationContext(), "LOST", Toast.LENGTH_LONG).show();
        round--;
        setButtonClickableStatus(false);
        game_finished = true;
        cancelCountDownTimer();
        youLostDialog();
        playSound(bp_sound[6],0);
        if(round > highScore){
            TextView hs = (TextView)findViewById(R.id.highScore);
            hs.setText("High Score:" + Integer.toString(round));

            try {
                FileOutputStream out = null;
                out = openFileOutput("highscore.txt", Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(out);
                BufferedWriter bw = new BufferedWriter(osw);
                PrintWriter pw = new PrintWriter(bw);
                pw.print(Integer.toString(round));
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        playersTurn = false;
        round = 1;
        tv.setText("Round: 1");
        simon = null;
    }
    class StartButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(simon == null){
                simon = new Player();
                //  player = new Player();
                round = 1;
                playerRound  = 1;
                simon.storeChoice(new Random().nextInt(4) + 0);
            }
            if (updateTask == null) {
                updateTask = new UpdateTask();
                updateTask.execute();
            } else {
                Log.i("INFO", "Update Task already running");
            }
        }
    }
    class StopButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            playersTurn = false;
            round = 1;
            tv.setText("Round: 1");
            simon = null;

        }
    }

    class UpdateTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            playersTurn = false;
            try {
                Thread.sleep(1000);
                int i = 0;
                while(round > i){
                    if(simon.getRound() > i){
                        int choice = simon.getChoice(i);
                        publishProgress(choice, i);
                    }else{
                        Random generator = new Random();
                        int choice = generator.nextInt(4) + 0;
                        simon.storeChoice(choice);
                        publishProgress(choice, i);
                    }
                    i++;
                    if(simon.getRound()<9){
                        Thread.sleep(1000);
                    }else if(simon.getRound()>=18){
                        Thread.sleep(700);
                    }else if(simon.getRound()<27){
                        Thread.sleep(500);
                    }else{
                        Thread.sleep(300);
                    }

                }
                round++;
                //publishProgress(-1, round);

            } catch (InterruptedException e) {
                Log.i("VALUES", "----- INTERRUPTED -----");
            } finally {
                playersTurn = true;
                playerRound = 1;
                publishProgress(-1, round);
                updateTask = null;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            setImagesToOff();

            switch (values[0]){
                case 0:
                    setImageToOn(0);
                    playSound(bp_sound[0],0);

                    break;
                case 1:
                    setImageToOn(1);
                    playSound(bp_sound[1],0);

                    break;
                case 2:
                    setImageToOn(2);
                    playSound(bp_sound[2],0);

                    break;
                case 3:
                    setImageToOn(3);
                    playSound(bp_sound[3],0);

                    break;
            }


            if(values[0]  == -1){
                tv.setText("Round: "+Integer.toString(values[1]-1));
                for (int i=0; i< 4; i++) {
                    findViewById(imageButtonId[i]).setClickable(true);
                }
                setCountDownTimer();
            }else{
                Log.i("SIMON", values[0].toString());
            }
        }
    }
    public void setImageToOn(int num){
        findViewById(imageButtonId[num]).setBackgroundResource(imagesOn[num]);
    }
    public void setImagesToOff(){
        for(int i=0; i< 4; i++) {
            findViewById(imageButtonId[i]).setBackgroundResource(imagesOff[i]);

        }
    }
    public void setButtonClickableStatus(boolean test){
        for (int i=0; i<4;i++) {
            findViewById(imageButtonId[i]).setClickable(test);
        }
    }
    private void playSound(int soundId, int loop) {
        if (soundsLoaded.contains(soundId)) {
            /*
                soundID 	    a soundID returned by the load() function
                leftVolume 	    left volume value (range = 0.0 to 1.0)
                rightVolume 	right volume value (range = 0.0 to 1.0)
                priority 	    stream priority (0 = lowest priority)
                loop 	        loop mode (0 = no loop, -1 = loop forever)
                rate 	        playback rate (1.0 = normal playback, range 0.5 to 2.0)
             */
            soundPool.play(soundId, 1f, .5f, 0, loop, 1.f);
        }
    }
    public void youLostDialog(){
        final RelativeLayout contentView = (RelativeLayout) ( this)
                .getLayoutInflater().inflate(R.layout.dialog, null);
        final ImageView image = (ImageView) contentView.findViewById(R.id.myanimation);
        final AnimationDrawable animation = (AnimationDrawable) image.getDrawable();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(contentView);

        builder.setCancelable(true);

        final AlertDialog dialog = builder.create();

        dialog.show();
        animation.start();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dialog.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 5000); // after 5 second (or 5000 miliseconds), the task will be active.
    }
    private void setCountDownTimer() {
        if( !isCounterRunning ){
            isCounterRunning = true;
            mCountDownTimer.start();
        }
        else{
            mCountDownTimer.cancel(); // cancel
            mCountDownTimer.start();  // then restart
        }

    }
    private void cancelCountDownTimer(){
        tv.setText(" ");
        mCountDownTimer.cancel();
    }
}
