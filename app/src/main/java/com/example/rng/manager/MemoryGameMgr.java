package com.example.rng.manager;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.example.rng.entity.MemoryUser;
import com.example.rng.R;
import com.example.rng.pages.MemoryGamePage;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MemoryGameMgr extends AppCompatActivity {
    // define constants and model instances
    private static final int max_sequence_size = 30;
    private static boolean buttonDisabled = false;

    private String gameDifficulty;

    protected MemorySequenceMgr expected = new MemorySequenceMgr(max_sequence_size);
    protected MemorySequenceMgr response = new MemorySequenceMgr();
    protected MemoryUser memoryUser = new MemoryUser();

    protected int level = 0;
    protected ArrayList<Button> buttonArray = new ArrayList<Button>();

    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        //get key
        if (extras != null) {
            level = extras.getInt("key");
            if(level == 1){
                memoryUser.setGameDifficulty("easy");
            }
            else{
                memoryUser.setGameDifficulty("hard");
            }
        }
        setContentView(R.layout.memory_game_page);

        // find buttons in view and set OnClick Listener
        Button backButton = findViewById(R.id.backButton);
        Button returnButton = findViewById(R.id.returnButton);
        backButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MemoryGameMgr.this, MemoryGamePage.class);
                startActivity(intent);
            }

        });
        returnButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MemoryGameMgr.this, MemoryGamePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
//                Intent intent = new Intent(MemoryGame.this, MemoryGameDifficulty.class);
//                startActivity(intent);
            }
        });

        // set text on view based off of model values
        TextView stageTxt = findViewById(R.id.stageTxt);
        TextView livesTxt = findViewById(R.id.livesTxt);
        TableLayout tableGrid = (TableLayout)findViewById(R.id.tableGrid);
        TextView playText = findViewById(R.id.playTextMem);
        Button playButton = findViewById(R.id.playButtonMem);
        livesTxt.setText("Lives: "+Integer.toString(memoryUser.getLives()));
        stageTxt.setText("Stage: "+Integer.toString(memoryUser.getStage()));

        // use level value to add buttons
        livesTxt.setVisibility(View.GONE);
        stageTxt.setVisibility(View.GONE);
        tableGrid.setVisibility(View.GONE);

        playButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
             playButton.setVisibility(View.GONE);
             playText.setVisibility(View.GONE);
             livesTxt.setVisibility(View.VISIBLE);
             stageTxt.setVisibility(View.VISIBLE);
             tableGrid.setVisibility(View.VISIBLE);
                int dimension = 0,gridsize =0;

                if (level == 1){
                    dimension = 3;
                    gridsize = 9;
                }
                else if (level == 2){
                    dimension = 4;
                    gridsize = 16;
                }
                addButtons(dimension);

                // generate random sequence that is of max_sequence_length and within the range 1-gridsize
                expected.generateRandomSequence(gridsize);
                // show the sequence
                showSequence(MemoryGameMgr.this.buttonArray, expected, memoryUser.getStage());
            }
        });


    }

    @SuppressLint("ClickableViewAccessibility")
    protected void addButtons(int dimension) {
        TableLayout tableGrid = (TableLayout)findViewById(R.id.tableGrid);
        for (int row =0; row < dimension; row++){
            // add a new row and set parameters
            TableRow tablerow = new TableRow(this);
            tablerow.setLayoutParams(new TableLayout.LayoutParams(

                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f));
            tablerow.setPadding(1,15,1,15);
            // add table row to tableGrid
            tableGrid.addView(tablerow);
            // populate table row with buttons and set parameters
            for(int col =0; col<dimension; col++){
                Button button = new Button(this);
                button.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f));
                button.setBackgroundResource(R.color.Gray);
                button.setId(((row)*dimension)+col+1);
                button.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event){
                        if(!buttonDisabled) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                button.setBackgroundResource(R.color.Gray);
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                button.setBackgroundResource(R.color.White);
                                gridButtonClicked(button.getId());
                            }
                        }
                        return false;
                    }
                });
                tablerow.addView(button);
                this.buttonArray.add(button);
                tablerow.addView(new TextView(this),10,button.getHeight());
            }
        }
    }


    protected void gridButtonClicked(int Id){
        // add new input into sequence
        response.addToSequence(Id);
        // compare both sequences
        int result = expected.compareTo(response);

        // find relevant view elements to edit based on result
        LinearLayout endGame = findViewById(R.id.endGame);
        TextView stagesClearedTxt = findViewById(R.id.stagesClearedTxt);
        TableLayout tableGrid = findViewById(R.id.tableGrid);

        if(result == -1){ // wrong user input
            response.clearSequence();
            memoryUser.setLives(memoryUser.getLives()-1);
            TextView livesTxt = findViewById(R.id.livesTxt);
            livesTxt.setText("Lives: "+Integer.toString(memoryUser.getLives()));
            if(memoryUser.getLives() == 0){ // no lives left
                buttonDisabled = true;
                memoryUser.storeStage();
                tableGrid.setVisibility(View.INVISIBLE);
                endGame.bringToFront();
                stagesClearedTxt.setText("Stages Complete:"+Integer.toString(memoryUser.getStage()));
                endGame.setVisibility(View.VISIBLE);
            }
            else { // life lost but still have lives left
                showSequence(this.buttonArray,expected, memoryUser.getStage());
            }
        }
        if(result == memoryUser.getStage()){ // stage cleared
            response.clearSequence();
            if(memoryUser.getStage() == max_sequence_size){ // finished all available stages
                buttonDisabled = true;
                memoryUser.storeStage();
                tableGrid.setVisibility(View.INVISIBLE);
                endGame.bringToFront();
                stagesClearedTxt.setText("Stages Complete:"+Integer.toString(memoryUser.getStage()));
                endGame.setVisibility(View.VISIBLE);
            }
            else { // move on next stage
                memoryUser.setStage(memoryUser.getStage()+1);
                TextView stageTxt = findViewById(R.id.stageTxt);
                stageTxt.setText("Stage: "+Integer.toString(memoryUser.getStage()));
                showSequence(this.buttonArray,expected, memoryUser.getStage());
            }
        }
    }

    protected void showSequence(ArrayList<Button> buttonArray, MemorySequenceMgr memorySequenceMgr, int stage){
        for(int i = 0; i< memoryUser.getStage(); i++){
            Button sequencebutton = buttonArray.get(memorySequenceMgr.getSequenceElement(i)-1);
            Timer timerRed = new Timer();
            // turn button red
            timerRed.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d("mytag", "disable");
                    buttonDisabled = true;
                    sequencebutton.setBackgroundResource(R.color.IndianRed);
                }
            }, (i+1)*1000);
            Timer timerBlue = new Timer();
            // turn button blue
            timerBlue.schedule(new TimerTask() {
                @Override
                public void run() {
                    sequencebutton.setBackgroundResource(R.color.Gray);
                }
            }, (i+1)*1000 +500);
        }
        Timer timerEnableButtons = new Timer();
        timerEnableButtons.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("mytag", "enable");
                buttonDisabled = false;
            }
        }, (memoryUser.getStage()+1)* 1000L);
    }
}

