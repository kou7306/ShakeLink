package com.example.myapplication;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView appNameTextView;
    private ImageView userIconImageView;
    private TextView matchingUserNameTextView;
    private Button loginButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // レイアウトからビューを取得
        appNameTextView = findViewById(R.id.appNameTextView);
        userIconImageView = findViewById(R.id.userIconImageView);
        matchingUserNameTextView = findViewById(R.id.matchingUserNameTextView);
        loginButton = findViewById(R.id.login_button);

        // マッチングしているユーザーの名前を取得して表示（仮に固定値を表示しています）
        String matchingUserName = "まだ交換したユーザーはいません"; // ここは実際に取得したユーザーの名前に置き換えてください
        matchingUserNameTextView.setText(matchingUserName);

        // ユーザーアイコンをクリックした際に、ユーザー情報画面に遷移する
        userIconImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyInfoActivity.class);
            startActivity(intent);
        });
        // ログインボタンをクリックした際に、ログイン画面に遷移する
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        // サービスの起動
        Intent bluetoothIntent = new Intent(this, BluetoothService.class);
        startService(bluetoothIntent);
        Intent DBAccessIntent = new Intent(this, DB_Access.class);
        startService(DBAccessIntent);
        Intent LoginIntent = new Intent(this, LoginActivity.class);
        startService(LoginIntent);
    }

}
