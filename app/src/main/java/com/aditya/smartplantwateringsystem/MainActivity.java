package com.aditya.smartplantwateringsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_PREFS="sharedPrefs";
    private boolean darkmodetoggle,automaticmodetoggle;
    private int moisturepercentage,minvalsp;
    private MaterialCardView materialcardView3;
    private TextView wateringstatustxt,MoisturePercentage;
    private Switch automodeswitch;
    private EditText minlvledittext;
    private ProgressBar MoistureprogressBar;
    private DatabaseReference moistureRef,motorRef,automaticRef,minvalRef;
    private String motorstate="off";
    private Button setbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadData();
        if(darkmodetoggle) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        Toolbar toolbar=findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        automodeswitch=findViewById(R.id.automodeswitch);
        minlvledittext=findViewById(R.id.minlvledittxt);
        MoistureprogressBar=findViewById(R.id.MoistureprogressBar);
        MoisturePercentage=findViewById(R.id.MoisturePercentage);
        materialcardView3=findViewById(R.id.materialCardView3);
        wateringstatustxt=findViewById(R.id.wateringstatustxt);
        setbtn=findViewById(R.id.setbtn);
        final LottieAnimationView watering=findViewById(R.id.wateringanim);
        if(automaticmodetoggle){
            minlvledittext.setText(minvalsp+"");automodeswitch.setChecked(true);minlvledittext.setFocusableInTouchMode(true);
            minlvledittext.setFocusable(true);setbtn.setVisibility(View.VISIBLE);
        }
        moistureRef=FirebaseDatabase.getInstance().getReference().child("Plant").child("Moisture");
        moistureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                moisturepercentage= Integer.parseInt(snapshot.getValue().toString());
                MoisturePercentage.setText(moisturepercentage+"%");
                MoistureprogressBar.setProgress(moisturepercentage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        motorRef=FirebaseDatabase.getInstance().getReference().child("Plant").child("Motor");
        automaticRef=FirebaseDatabase.getInstance().getReference().child("Plant").child("Automatic");
        minvalRef=FirebaseDatabase.getInstance().getReference().child("Plant").child("Minval");
        watering.setSpeed((float)0.75);
        materialcardView3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    motorstate="on";
                    motorRef.setValue(motorstate);
                    wateringstatustxt.setText("Release to Stop");
                    watering.resumeAnimation();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    motorstate="off";
                    motorRef.setValue(motorstate);
                    wateringstatustxt.setText("Hold to Water the Plant");
                    watering.pauseAnimation();
                }
                return false;
            }
        });
        automodeswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    minlvledittext.setFocusableInTouchMode(true);minlvledittext.setFocusable(true);setbtn.setVisibility(View.VISIBLE);
                }
                else{
                    minlvledittext.setText("");
                    minlvledittext.setFocusableInTouchMode(false);minlvledittext.setFocusable(false);
                    setbtn.setVisibility(View.GONE);
                    automaticRef.setValue(false);minvalRef.setValue((long)0);
                    Toast.makeText(MainActivity.this, "Automatic Mode Disabled!", Toast.LENGTH_SHORT).show();
                    minvalsp=0;automaticmodetoggle=false;
                    saveData();
                }
            }
        });
        setbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(minlvledittext.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Please Enter a minimum value", Toast.LENGTH_SHORT).show();
                }
                else {
                    automaticRef.setValue(true);
                    long minvalpercent = Long.parseLong(minlvledittext.getText().toString());
                    minvalRef.setValue(minvalpercent);
                    Toast.makeText(MainActivity.this, "Automatic Mode Enabled!", Toast.LENGTH_SHORT).show();
                    minvalsp=(int)minvalpercent;automaticmodetoggle=true;
                    saveData();
                }
            }
        });
       /* minlvledittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                minmoistureval=Integer.parseInt(charSequence.toString());saveData();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.my_menu,menu);
        if (darkmodetoggle) {
            menu.findItem(R.id.enable_dark_mode_button).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.enable_dark_mode_button:
                if(item.isChecked()){
                    item.setChecked(false);
                    darkmodetoggle=false;
                    saveData();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                else{
                    item.setChecked(true);
                    darkmodetoggle=true;
                    saveData();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }
    public void saveData(){
        SharedPreferences sharedPreferences=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("enabledarkmode",darkmodetoggle);
        editor.putBoolean("automaticmodetoggle",automaticmodetoggle);
        editor.putInt("minimumvalue",minvalsp);
        editor.apply();
    }
    public void loadData(){
        SharedPreferences sharedPreferences=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        darkmodetoggle=sharedPreferences.getBoolean("enabledarkmode",false);
        automaticmodetoggle=sharedPreferences.getBoolean("automaticmodetoggle",false);
        minvalsp=sharedPreferences.getInt("minimumvalue",0);
    }
}