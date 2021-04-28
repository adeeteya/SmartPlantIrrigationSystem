package com.aditya.smartplantwateringsystem;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";
    private boolean darkmodetoggle, automaticmodetoggle, notificationtoggle;
    private int moisturepercentage, minvalsp;
    private TextView wateringstatustxt, moisturePercentagetxt;
    private EditText minlvledittext;
    private ProgressBar moistureProgressBar;
    private DatabaseReference motorRef;
    private DatabaseReference automaticRef;
    private DatabaseReference minvalRef;
    private DatabaseReference bypassRef;
    private String motorstate = "off";
    private Button setbtn;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadData();
        if (darkmodetoggle) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (notificationtoggle) {
            startService(new Intent(this, backgroundNotificationService.class));
        } else {
            stopService(new Intent(this, backgroundNotificationService.class));
        }
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        SwitchMaterial automodeswitch = findViewById(R.id.automodeswitch);
        minlvledittext = findViewById(R.id.minlvledittxt);
        moistureProgressBar = findViewById(R.id.moistureProgressBar);
        moisturePercentagetxt = findViewById(R.id.MoisturePercentage);
        MaterialCardView materialcardView = findViewById(R.id.materialCardView);
        MaterialCardView materialcardView3 = findViewById(R.id.materialCardView3);
        wateringstatustxt = findViewById(R.id.wateringstatustxt);
        setbtn = findViewById(R.id.setbtn);
        final LottieAnimationView watering = findViewById(R.id.wateringanim);
        if (automaticmodetoggle) {
            minlvledittext.setText(String.valueOf(minvalsp));
            automodeswitch.setChecked(true);
            minlvledittext.setFocusableInTouchMode(true);
            minlvledittext.setFocusable(true);
            setbtn.setVisibility(View.VISIBLE);
        }
        DatabaseReference moistureRef = FirebaseDatabase.getInstance().getReference().child("Plant").child("Moisture");
        moistureRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                moisturepercentage = Integer.parseInt(snapshot.getValue().toString());
                moisturePercentagetxt.setText(moisturepercentage + "%");
                moistureProgressBar.setProgress(moisturepercentage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        motorRef = FirebaseDatabase.getInstance().getReference().child("Plant").child("Motor");
        automaticRef = FirebaseDatabase.getInstance().getReference().child("Plant").child("Automatic");
        minvalRef = FirebaseDatabase.getInstance().getReference().child("Plant").child("Minval");
        bypassRef = FirebaseDatabase.getInstance().getReference().child("Plant").child("Bypass");
        watering.setSpeed((float) 0.75);
        materialcardView.setOnClickListener(view -> bypassRef.setValue("yes"));
        materialcardView3.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                motorstate = "on";
                motorRef.setValue(motorstate);
                wateringstatustxt.setText(R.string.releaseTxt);
                watering.resumeAnimation();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                motorstate = "off";
                motorRef.setValue(motorstate);
                wateringstatustxt.setText(R.string.wateringTxt);
                watering.pauseAnimation();
            }
            return false;
        });
        automodeswitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                minlvledittext.setFocusableInTouchMode(true);
                minlvledittext.setFocusable(true);
                setbtn.setVisibility(View.VISIBLE);
            } else {
                minlvledittext.setText("");
                minlvledittext.setFocusableInTouchMode(false);
                minlvledittext.setFocusable(false);
                setbtn.setVisibility(View.GONE);
                automaticRef.setValue("no");
                minvalRef.setValue((long) 0);
                Toast.makeText(MainActivity.this, "Automatic Mode Disabled!", Toast.LENGTH_SHORT).show();
                minvalsp = 0;
                automaticmodetoggle = false;
                saveData();
            }
        });
        setbtn.setOnClickListener(view -> {
            if (minlvledittext.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "Please Enter a minimum value", Toast.LENGTH_SHORT).show();
            } else {
                automaticRef.setValue("yes").addOnSuccessListener(aVoid -> {
                    long minvalpercent = Long.parseLong(minlvledittext.getText().toString());
                    minvalRef.setValue(minvalpercent);
                    Toast.makeText(MainActivity.this, "Automatic Mode Enabled!", Toast.LENGTH_SHORT).show();
                    minvalsp = (int) minvalpercent;
                    automaticmodetoggle = true;
                    saveData();
                }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error Occurred,Please check your internet connection", Toast.LENGTH_SHORT).show());
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.my_menu,menu);
        if (darkmodetoggle) {
            menu.findItem(R.id.enable_dark_mode_button).setChecked(true);
        }
        menu.findItem(R.id.enable_notifications).setChecked(notificationtoggle);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.enable_dark_mode_button:
                if (item.isChecked()) {
                    item.setChecked(false);
                    darkmodetoggle = false;
                    saveData();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    item.setChecked(true);
                    darkmodetoggle = true;
                    saveData();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                return true;
            case R.id.enable_notifications:
                if (item.isChecked()) {
                    item.setChecked(false);
                    notificationtoggle = false;
                    saveData();
                    stopService(new Intent(this, backgroundNotificationService.class));
                    Toast.makeText(this, "Please restart the app!", Toast.LENGTH_SHORT).show();
                } else {
                    item.setChecked(true);
                    notificationtoggle = true;
                    saveData();
                    startService(new Intent(this, backgroundNotificationService.class));
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("enabledarkmode", darkmodetoggle);
        editor.putBoolean("automaticmodetoggle", automaticmodetoggle);
        editor.putBoolean("notificationtoggle", notificationtoggle);
        editor.putInt("minimumvalue", minvalsp);
        editor.apply();
    }
    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        darkmodetoggle = sharedPreferences.getBoolean("enabledarkmode", false);
        automaticmodetoggle = sharedPreferences.getBoolean("automaticmodetoggle", false);
        notificationtoggle = sharedPreferences.getBoolean("notificationtoggle", true);
        minvalsp = sharedPreferences.getInt("minimumvalue", 0);
    }

}