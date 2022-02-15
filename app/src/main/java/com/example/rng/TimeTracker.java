package com.example.rng;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeTracker {
    private String gameLevel;

    static void storeTime(double timeTaken, String gameLevel, String game) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        if (user != null) {
            uid = user.getUid();
        }
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("allScores");
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        Record record = new Record(date, timeTaken);

        rootRef.child(uid).child(game).child(gameLevel).push().setValue(record);
    }
}