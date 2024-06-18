package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText;
    private Button saveButton;
    private TextView displayTextView;
    private DB_Access dbAccess;
    private boolean isBound = false;
    private FirebaseAuth mAuth;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DB_Access.LocalBinder binder = (DB_Access.LocalBinder) service;
            dbAccess = binder.getService();
            isBound = true;

            // Get user data after binding
            getUserData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.nameEditText);
        ageEditText = findViewById(R.id.ageEditText);
        saveButton = findViewById(R.id.saveButton);
        displayTextView = findViewById(R.id.displayTextView);

        mAuth = FirebaseAuth.getInstance();
        signInAnonymously();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        Intent serviceIntent = new Intent(this, DB_Access.class);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Wait until service is bound before setting userId
                            if (isBound) {
                                dbAccess.setUserId(user.getUid());
                                getUserData();
                            } else {
                                // Retry setting userId and getting data once the service is bound
                                ServiceConnection retryConnection = new ServiceConnection() {
                                    @Override
                                    public void onServiceConnected(ComponentName name, IBinder service) {
                                        DB_Access.LocalBinder binder = (DB_Access.LocalBinder) service;
                                        dbAccess = binder.getService();
                                        isBound = true;
                                        dbAccess.setUserId(user.getUid());
                                        getUserData();
                                    }

                                    @Override
                                    public void onServiceDisconnected(ComponentName name) {
                                        isBound = false;
                                    }
                                };
                                bindService(new Intent(this, DB_Access.class), retryConnection, BIND_AUTO_CREATE);
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData() {
        String name = nameEditText.getText().toString();
        String age = ageEditText.getText().toString();

        if (name.isEmpty() || age.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter both name and age", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBound) {
            dbAccess.saveUserData(name, age, new DB_Access.DataStatus() {
                @Override
                public void DataIsLoaded(String name, String age) {
                    // Not used for save operation
                }

                @Override
                public void DataIsInserted() {
                    Toast.makeText(MainActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                    getUserData();
                }

                @Override
                public void DataIsUpdated() {
                    // Not used for save operation
                }

                @Override
                public void DataIsDeleted() {
                    // Not used for save operation
                }
            });
        }
    }

    private void getUserData() {
        if (isBound && mAuth.getCurrentUser() != null) {
            dbAccess.getUserData(new DB_Access.DataStatus() {
                @Override
                public void DataIsLoaded(String name, String age) {
                    displayTextView.setText("Name: " + name + "\nAge: " + age);
                }

                @Override
                public void DataIsInserted() {
                    // Not used for get operation
                }

                @Override
                public void DataIsUpdated() {
                    // Not used for get operation
                }

                @Override
                public void DataIsDeleted() {
                    // Not used for get operation
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}
