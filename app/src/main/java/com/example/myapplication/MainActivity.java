package com.example.myapplication;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView appNameTextView;
    private ImageView userIconImageView;
    private TextView matchingUserNameTextView;
    private FirebaseFirestore db;
    private String currentUserId = "5Da7IxIOlbOsQLBChedt"; // ここは実際のユーザーIDに置き換えてください
    private List<User> userList;
    private UserAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // レイアウトからビューを取得
        appNameTextView = findViewById(R.id.appNameTextView);
        userIconImageView = findViewById(R.id.userIconImageView);

        // Firebase Firestoreのインスタンスを取得
        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // マッチングしているユーザーの名前を取得して表示
        fetchMatchingUserNames();

        // ユーザーアイコンをクリックした際に、ユーザー情報画面に遷移する
        userIconImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyInfoActivity.class);
            startActivity(intent);
        });
        // サービスの起動
        Intent bluetoothIntent = new Intent(this, BluetoothService.class);
        startService(bluetoothIntent);
        Intent DBAccessIntent = new Intent(this, DB_Access.class);
        startService(DBAccessIntent);
    }

    private void fetchMatchingUserNames() {
        ArrayList<String> matchedUserIds = new ArrayList<>();

        db.collection("matching")
                .whereEqualTo("user1", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String matchedUserId = document.getString("user2");
                            if (matchedUserId != null) {
                                matchedUserIds.add(matchedUserId);
                            }
                        }
                        fetchUserNames(matchedUserIds);
                    } else {
                        matchingUserNameTextView.setText("データの取得に失敗しました");
                    }
                });

        db.collection("matching")
                .whereEqualTo("user2", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String matchedUserId = document.getString("user1");
                            if (matchedUserId != null) {
                                matchedUserIds.add(matchedUserId);
                            }
                        }
                        fetchUserNames(matchedUserIds);
                    } else {
                        matchingUserNameTextView.setText("データの取得に失敗しました");
                    }
                });
    }

    private void fetchUserNames(ArrayList<String> userIds) {
        StringBuilder userNames = new StringBuilder();
        for (String userId : userIds) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userName = task.getResult().getString("name");
                            Integer age = task.getResult().getLong("age").intValue();
                            Log.d("MainActivity", "userName: " + userName + ", age: " + age + "id: " + userId);
                            userNames.append(userName != null ? userName : "名前が設定されていません").append("\n");
                            User user = new User(userId , age, userName);
                            userList.add(user);
                            adapter.notifyDataSetChanged();
                        } else {
                            matchingUserNameTextView.setText("ユーザー情報が見つかりません");
                        }
                    });
        }
    }
}

