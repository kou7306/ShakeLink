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

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    private BluetoothAdapter bluetoothAdapter;
    private Context mContext; // Contextを保持するメンバ変数
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

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {

                    // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
                    if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("BluetoothService", "Bluetooth permission not granted");
                        return;
                    }

                    Log.d("BluetoothService", "Found device:Log.d(" + device.getName() + " (" + device.getAddress() + ")");
                    // 権限を確認してからデバイスとの接続を開始する
                    if (context.checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        connectToDevice(device);
                    } else {
                        Log.e("BluetoothService", "Permissions not granted.");
                    }
                }
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    // デバイスとのペアリングを試みる
                    pairWithDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("BluetoothService", "Bluetooth discovery started.");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("BluetoothService", "Bluetooth discovery finished.");
            }
        }

        private void connectToDevice(BluetoothDevice device) {
            try {
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BluetoothService", "Bluetooth permission not granted");
                    return;
                }
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                receiveData(socket);
            } catch (IOException e) {
                Log.e("BluetoothService", "Failed to connect to device", e);
            }
        }

        private void pairWithDevice(BluetoothDevice device) {
            try {
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BluetoothService", "Bluetooth permission not granted");
                    return;
                }
                device.setPin("1234".getBytes()); // 仮のPINコードを設定
                // ペアリングが成功するとBluetoothDevice.ACTION_BOND_STATE_CHANGEDアクションが送信される
            } catch (Exception e) {
                Log.e("BluetoothService", "Failed to pair with device", e);
            }
        }

        private void receiveData(BluetoothSocket socket) {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;
                while (true) {
                    bytes = inputStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    String receivedData = new String(buffer, 0, bytes);
                    Log.d("BluetoothService", "Received data: " + receivedData);
                }
                socket.close();
            } catch (IOException e) {
                Log.e("BluetoothService", "Failed to receive data", e);
            }
        }
    };

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
