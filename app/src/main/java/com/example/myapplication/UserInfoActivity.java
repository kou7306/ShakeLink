package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {
    private TextView textViewName;
    private TextView textViewAge;
    private TextView textViewGender;
    private TextView textViewAffiliation;
    private TextView textViewHobbies;
    private TextView textViewSNSLink;
    private TextView textViewComment;
    private TextView textViewMacAddress;
    private Button buttonSave;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        // レイアウトからビューを取得
        textViewName = findViewById(R.id.textViewName);
        textViewAge = findViewById(R.id.textViewAge);
        textViewGender = findViewById(R.id.textViewGender);
        textViewAffiliation = findViewById(R.id.textViewAffiliation);
        textViewHobbies = findViewById(R.id.textViewHobbies);
        textViewSNSLink = findViewById(R.id.textViewSNSLink);
        textViewComment = findViewById(R.id.textViewComment);
        textViewMacAddress = findViewById(R.id.textViewMacAddress);
        buttonSave = findViewById(R.id.buttonSave);

        // SharedPreferencesからユーザーIDを取得
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        // インテントからデータを取得して表示
        Intent intent = getIntent();
        textViewName.setText(intent.getStringExtra("name"));
        textViewAge.setText(intent.getStringExtra("age"));
        textViewGender.setText(intent.getStringExtra("gender"));
        textViewAffiliation.setText(intent.getStringExtra("affiliation"));
        textViewHobbies.setText(intent.getStringExtra("hobbies"));
        textViewSNSLink.setText(intent.getStringExtra("snsLink"));
        textViewComment.setText(intent.getStringExtra("comment"));
        textViewMacAddress.setText(intent.getStringExtra("macAddress"));
        // 保存ボタンのクリックリスナーを設定
        buttonSave.setOnClickListener(view -> saveUserDataToFirebase());
    }

    private void saveUserDataToFirebase() {
        String name = textViewName.getText().toString();
        String age = textViewAge.getText().toString();
        String gender = textViewGender.getText().toString();
        String affiliation = textViewAffiliation.getText().toString();
        String hobbies = textViewHobbies.getText().toString();
        String snsLink = textViewSNSLink.getText().toString();
        String comment = textViewComment.getText().toString();
        String macAddress = textViewMacAddress.getText().toString();

        Map<String, Object> userData = new HashMap<>();
        userData.put("user", userId);

        // FirebaseFirestoreインスタンスを取得
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestoreにデータを保存
        db.collection("matching")
                .document()
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // 保存成功時にToastメッセージを表示
                    Toast.makeText(UserInfoActivity.this, "保存されました", Toast.LENGTH_SHORT).show();

                    // MainActivityに戻る
                    Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // 保存失敗時にログ出力（必要ならエラーハンドリング）
                    e.printStackTrace();
                });
    }
}
