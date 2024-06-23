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
import android.os.Handler;
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
import java.util.ArrayList;

public class BluetoothService extends Service {
    private Handler mHandler;
    private Context mContext;
    private static final int INTERVAL = 5000; // 5秒ごとに実行
    private BluetoothAdapter bluetoothAdapter;
    private boolean isDiscovering = false;
    private FirebaseFirestore firestore;
    private static final String TAG = "BluetoothService";
    private ArrayList<String> macAddressList;
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mContext = this; // Contextを取得
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        firestore = FirebaseFirestore.getInstance();
        // MACアドレスを格納するArrayListの初期化
        macAddressList = new ArrayList<>();

        // ブロードキャストレシーバーを登録
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
        getAllMacAddressFromFirestore();

        startRepeatingTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        // ブロードキャストレシーバーを解除
        unregisterReceiver(bluetoothReceiver);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            startBluetoothDiscovery();
            mHandler.postDelayed(this, INTERVAL);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void startBluetoothDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "ACCESS_FINE_LOCATION permission not granted");
                return;
            }

            bluetoothAdapter.startDiscovery();
        }
    }

    private void stopBluetoothDiscovery() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "ACCESS_FINE_LOCATION permission not granted");
            return;
        }

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            stopRepeatingTask();
        }
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
                    if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "ACCESS_FINE_LOCATION permission not granted");
                        return;
                    }

                    String macAddress = device.getAddress();
                    Log.d(TAG, "Found device: " + device.getName() + " (" + macAddress + ")");
                    if (macAddressList.contains(macAddress)) {
                        // MACアドレスがFirestoreに登録されている場合
                        Log.d(TAG, "MAC address found in Firestore: " + macAddress);
                        // Firebaseからユーザー情報を取得
                        getUserInfoFromFirebase(macAddress, context);
                    } else {
                        // MACアドレスがFirestoreに登録されていない場合
                        Log.d(TAG, "MAC address not found in Firestore: " + macAddress);
                    }
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
                                    Log.d("BluetoothService", "User data: " + userData.toString());
                                    Intent intent = new Intent(context, UserInfoActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    for (Map.Entry<String, Object> entry : userData.entrySet()) {
                                        intent.putExtra(entry.getKey(), entry.getValue().toString());
                                    }
                                    stopBluetoothDiscovery();
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


    private void getAllMacAddressFromFirestore() {
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String macAddress = document.getString("macAddress");
                            macAddressList.add(macAddress);
                            Log.d(TAG, "MAC Address: " + macAddress);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

