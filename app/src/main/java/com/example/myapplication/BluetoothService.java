package com.example.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.UUID;

public class BluetoothService extends Service {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private Context mContext;

    // UUID for the BLE service and characteristic
    private static final UUID SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB"); // Replace with your service UUID
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB"); // Replace with your characteristic UUID

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e("BluetoothService", "BLE not supported on this device.");
            stopSelf();
            return;
        }

        if (!checkPermissions()) {
            Log.e("BluetoothService", "Permissions not granted.");
            stopSelf();
            return;
        }

        initializeBluetooth();
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeBluetooth() {
        Log.d("BluetoothService", "Initializing Bluetooth");
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.d("BluetoothService", "Bluetooth permission not granted");
                return;
            }
            startActivity(enableBtIntent);
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        startBleScan();
    }

    private void startBleScan() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothLeScanner.startScan(bleScanCallback);
    }

    private final ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.d("BluetoothService", "Bluetooth permission not granted");
                return;
            }
            Log.d("BluetoothService", "Found BLE device: " + device.getName() + " (" + device.getAddress() + ")");
            connectToDevice(device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                BluetoothDevice device = result.getDevice();
                // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BluetoothService", "Bluetooth permission not granted");
                    return;
                }
                Log.d("BluetoothService", "Found BLE device: " + device.getName() + " (" + device.getAddress() + ")");
                connectToDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BluetoothService", "BLE scan failed with error code: " + errorCode);
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothService", "Bluetooth permission not granted");
            return;
        }
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("BluetoothService", "Connected to BLE device");
                // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("BluetoothService", "Bluetooth permission not granted");
                    return;
                }
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d("BluetoothService", "Disconnected from BLE device");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
                        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                            Log.d("BluetoothService", "Bluetooth permission not granted");
                            return;
                        }
                        gatt.readCharacteristic(characteristic);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String data = new String(characteristic.getValue());
                Log.d("BluetoothService", "Received data: " + data);
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
        if (bluetoothGatt != null) {
            // デバイスを検出した際にログにデバイスの名前とアドレスを出力する
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.d("BluetoothService", "Bluetooth permission not granted");
                return;
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}