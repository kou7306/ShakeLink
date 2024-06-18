package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class DB_Access extends Service {

    private final IBinder binder = new LocalBinder();
    private FirebaseFirestore db;
    private String userId;

    public class LocalBinder extends Binder {
        DB_Access getService() {
            return DB_Access.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void saveUserData(String name, String age, final DataStatus dataStatus) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("age", age);

        if (userId == null) {
            Log.w(TAG, "userId is null. Cannot save data.");
            return;
        }

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    dataStatus.DataIsInserted();
                } else {
                    Log.w(TAG, "Error saving document", task.getException());
                }
            }
        });
    }

    public void getUserData(final DataStatus dataStatus) {
        if (userId == null) {
            Log.w(TAG, "userId is null. Cannot get data.");
            return;
        }

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String age = document.getString("age");
                        dataStatus.DataIsLoaded(name, age);
                    } else {
                        Log.w(TAG, "No such document");
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface DataStatus {
        void DataIsLoaded(String name, String age);
        void DataIsInserted();
        void DataIsUpdated();
        void DataIsDeleted();
    }
}
