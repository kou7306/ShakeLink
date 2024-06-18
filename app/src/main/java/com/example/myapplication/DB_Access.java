package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.util.Log;
import java.util.Map;
import static android.content.ContentValues.TAG;

public class DB_Access extends Service {

    private FirebaseFirestore db;
    private EventListener<QuerySnapshot> eventListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            for (QueryDocumentSnapshot document : value) {
                Log.d(TAG, document.getId() + " => " + document.getData());
            }
        }
    };

    private OnCompleteListener<Void> getWriteCompleteListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Document successfully written!");
                } else {
                    Log.w(TAG, "Error writing document", task.getException());
                }
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        fetchDataFromFirestore();
    }

    // DBからデータを取得する関数
    public void fetchDataFromFirestore() {
        db.collection("users").addSnapshotListener(eventListener);
    }
    // DBにデータを挿入する関数
    public void saveDataToFirestore(Map<String, Object> data) {
        db.collection("users").document()
                .set(data)
                .addOnCompleteListener(getWriteCompleteListener());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
