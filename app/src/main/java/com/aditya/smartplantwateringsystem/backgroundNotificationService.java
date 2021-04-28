package com.aditya.smartplantwateringsystem;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class backgroundNotificationService extends Service {
    private int moisturepercentage = 100;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("plantnotification", "moistureChannel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel for smart plant watering system notification");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        DatabaseReference moistureRef = FirebaseDatabase.getInstance().getReference().child("Plant").child("Moisture");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intentz = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "plantnotification")
                .setSmallIcon(R.drawable.ic_baseline_warning_24)
                .setContentTitle("Moisture Level is Low")
                .setContentIntent(intentz)
                .setContentText("Please water the plant")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        moistureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                moisturepercentage = Integer.parseInt(snapshot.getValue().toString());
                if (moisturepercentage <= 15) {
                    notificationManager.notify(100, builder.build());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return START_STICKY;
    }

}
