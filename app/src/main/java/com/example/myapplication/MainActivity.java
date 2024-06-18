package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DB_Access dbAccess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(this, DB_Access.class);
        startService(serviceIntent);
        // BluetoothServiceを起動するIntentを作成
        Intent bluetoothServiceIntent = new Intent(this, BluetoothService.class);

        // BluetoothServiceを開始
        startService(bluetoothServiceIntent);

        dbAccess = new DB_Access();
        dbAccess.onCreate();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }


    public void onSaveButtonClick(View view) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "John Doe");
        userData.put("age", 30);

        dbAccess.saveDataToFirestore(userData);
    }

}