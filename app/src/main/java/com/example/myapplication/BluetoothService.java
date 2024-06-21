package com.example.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public class BluetoothService extends Service {
    private BluetoothAdapter bluetoothAdapter;
    private Context mContext; // Contextを保持するメンバ変数
    private ArrayList<String> macAddressList = new ArrayList<>();
    // Serial Port Profile (SPP) UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this; // Contextを取得

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // デバイスがBluetoothをサポートしていない場合の処理
            Log.e("BluetoothService", "Bluetooth is not supported on this device.");
            stopSelf();
            return;
        }

        // Bluetoothおよび位置情報の権限のチェック
        if (!checkPermissions()) {
            // 権限がない場合はサービスを停止
            Log.e("BluetoothService", "Permissions not granted.");
            stopSelf();
            return;
        }

        initializeBluetooth();
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeBluetooth() {
        Log.d("BluetoothService", "start initializeBluetooth");
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.e("BluetoothService", "Permission denied for Bluetooth");
                return;
            }
            startActivity(enableBtIntent);
        }

        // Bluetoothデバイスの検出を開始
        startBluetoothDiscovery();
    }

    

    // Bluetoothデバイスの検出を開始
    private void startBluetoothDiscovery() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);

        try {
            bluetoothAdapter.startDiscovery();
        } catch (SecurityException e) {
            Log.e("BluetoothService", "Permission denied for startDiscovery", e);
        }
    }

    // Bluetoothデバイスが検出されたときの処理
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    // MACアドレスを取得
                    // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
                    if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("BluetoothService", "Bluetooth permission not granted");
                        return;
                    }

                    String macAddress = device.getAddress();
                    Log.d("BluetoothService", "Found device:Log.d(" + device.getName() + " (" + device.getAddress() + ")");

                    // Firebaseからユーザー情報を取得
                    getUserInfoFromFirebase(macAddress, context);
                }
            }
        }
    };

    // Firebaseからユーザー情報を取得するメソッド
    private void getUserInfoFromFirebase(String macAddress, Context context) {
        // Firebaseのデータベース参照
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // MACアドレスに一致するドキュメントを取得するためのクエリを作成
        db.collection("users")
                .whereEqualTo("macAddress", macAddress)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // ユーザー情報が存在する場合は新しいアクティビティにデータを渡して起動
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    Map<String, Object> userData = document.getData();
                                    Intent intent = new Intent(context, UserInfoActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    for (Map.Entry<String, Object> entry : userData.entrySet()) {
                                        intent.putExtra(entry.getKey(), entry.getValue().toString());
                                    }
                                    context.startActivity(intent);
                                }
                            } else {
                                // ユーザー情報が存在しない場合はログに出力
                                Log.d("BluetoothService", "No such document");
                            }
                        } else {
                            // Firebaseからのデータ取得に失敗した場合はログに出力
                            Log.e("BluetoothService", "Error getting document", task.getException());
                        }
                    }
                });
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.e("BluetoothService", "Permission denied for Bluetooth");
                return;
            }
            bluetoothAdapter.cancelDiscovery();
        }
    }
}
