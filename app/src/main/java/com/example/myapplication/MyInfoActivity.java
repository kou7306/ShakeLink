package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyInfoActivity extends AppCompatActivity {
    private TextView textViewName;
    private TextView textViewAge;
    private TextView textViewGender;
    private TextView textViewAffiliation;
    private TextView textViewHobbies;
    private TextView textViewSNSLink;
    private TextView textViewComment;
    private TextView textViewMacAddress;
    private Button buttonEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        // レイアウトからビューを取得
        textViewName = findViewById(R.id.textViewName);
        textViewAge = findViewById(R.id.textViewAge);
        textViewGender = findViewById(R.id.textViewGender);
        textViewAffiliation = findViewById(R.id.textViewAffiliation);
        textViewHobbies = findViewById(R.id.textViewHobbies);
        textViewSNSLink = findViewById(R.id.textViewSNSLink);
        textViewComment = findViewById(R.id.textViewComment);
        textViewMacAddress = findViewById(R.id.textViewMacAddress);
        buttonEdit = findViewById(R.id.buttonEdit);

        // Firebaseからデータを取得して表示
        getUserInfoFromFirebase();

        // 編集ボタンのクリックリスナーを設定
        buttonEdit.setOnClickListener(v -> {
            // log
            Log.d("MyInfoActivity", "Edit button clicked");
            Intent intent = new Intent(MyInfoActivity.this, UserRegisterActivity.class);
            startActivity(intent);
        });
    }

    private void getUserInfoFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String macAddress = "WVwrZzxsm0iK586xSaK1";  // 実際のMACアドレスに置き換えてください
        DocumentReference docRef = db.collection("users").document(macAddress);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        textViewName.setText("名前: " + document.getString("name"));
                        textViewAge.setText("年齢: " + document.getLong("age").intValue());
                        textViewGender.setText("性別: " + document.getString("gender"));
                        textViewAffiliation.setText("所属: " + document.getString("affiliation"));
                        textViewHobbies.setText("趣味: " + document.getString("hobbies"));
                        textViewSNSLink.setText("SNSリンク: " + document.getString("snsLink"));
                        textViewComment.setText("一言: " + document.getString("comment"));
                        textViewMacAddress.setText("MACアドレス: " + document.getString("macAddress"));
                    } else {
                        Log.d("DisplayUserInfoActivity", "No such document");
                    }
                } else {
                    Log.e("DisplayUserInfoActivity", "Error getting document", task.getException());
                }
            }
        });
    }
}
