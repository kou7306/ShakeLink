package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private Spinner spinnerAge;
    private RadioGroup radioGroupGender;
    private EditText editTextAffiliation;
    private EditText editTextHobbies;
    private EditText editTextSNSLink;
    private EditText editTextComment;
    private EditText editTextMacAddress;
    private Button buttonSubmit;
    private String userId;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        // Firestoreインスタンスの初期化
        db = FirebaseFirestore.getInstance();

        // SharedPreferencesからユーザーIDを取得
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        Log.d("UserRegisterActivity", "userId: " + userId);

        // レイアウトからビューを取得
        editTextName = findViewById(R.id.editTextName);
        spinnerAge = findViewById(R.id.spinnerAge);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        editTextAffiliation = findViewById(R.id.editTextAffiliation);
        editTextHobbies = findViewById(R.id.editTextHobbies);
        editTextSNSLink = findViewById(R.id.editTextSNSLink);
        editTextComment = findViewById(R.id.editTextComment);
        editTextMacAddress = findViewById(R.id.editTextMacAddress);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // 年齢の範囲を設定
        Integer[] ages = new Integer[100];
        for (int i = 0; i < 100; i++) {
            ages[i] = i + 1;
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(adapter);

        // ボタンのクリックリスナーを設定
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFirestore();
            }
        });

        // 既存データがある場合、それを表示
        loadDataFromFirestore();
    }

    private void loadDataFromFirestore() {
        // Firestoreから既存データを取得
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // 既存データがあれば、フィールドに設定
                    editTextName.setText(document.getString("name"));
                    spinnerAge.setSelection(document.getLong("age").intValue() - 1);
                    String gender = document.getString("gender");
                    if (gender != null) {
                        switch (gender) {
                            case "男性":
                                radioGroupGender.check(R.id.radioMale);
                                break;
                            case "女性":
                                radioGroupGender.check(R.id.radioFemale);
                                break;
                            case "その他":
                                radioGroupGender.check(R.id.radioOther);
                                break;
                        }
                    }
                    editTextAffiliation.setText(document.getString("affiliation"));
                    editTextHobbies.setText(document.getString("hobbies"));
                    editTextSNSLink.setText(document.getString("snsLink"));
                    editTextComment.setText(document.getString("comment"));
                    editTextMacAddress.setText(document.getString("macAddress"));
                }
            }
        });
    }

    private void saveDataToFirestore() {
        // ユーザーの入力内容を取得
        String name = editTextName.getText().toString();
        int age = (int) spinnerAge.getSelectedItem();
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
        String gender = selectedGenderRadioButton.getText().toString();
        String affiliation = editTextAffiliation.getText().toString();
        String hobbies = editTextHobbies.getText().toString();
        String snsLink = editTextSNSLink.getText().toString();
        String comment = editTextComment.getText().toString();
        String macAddress = editTextMacAddress.getText().toString();

        // Firestoreに保存するデータを作成
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", age);
        userData.put("gender", gender);
        userData.put("affiliation", affiliation);
        userData.put("hobbies", hobbies);
        userData.put("snsLink", snsLink);
        userData.put("comment", comment);
        userData.put("macAddress", macAddress);

        Log.d("UserRegisterActivity", "userId: " + userId);

        // Firestoreにデータを保存
        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // データ保存成功
                    // 保存成功時にToastメッセージを表示
                    Toast.makeText(UserRegisterActivity.this, "保存されました", Toast.LENGTH_SHORT).show();
                    // MainActivityに戻る
                    Intent intent = new Intent(UserRegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // データ保存失敗
                    // 必要に応じてユーザーに通知などを行う
                    Toast.makeText(UserRegisterActivity.this, "失敗しました", Toast.LENGTH_SHORT).show();
                });
    }
}
