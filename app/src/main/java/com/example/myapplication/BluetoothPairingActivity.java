package com.example.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BluetoothPairingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                showPairingRequest(device);
            }
        }
    }

    private void showPairingRequest(BluetoothDevice device) {
//        // ここでペアリングリクエストを表示するUIを実装します。
//        // 例えば、ダイアログを表示してユーザーに承認を求めることができます。
//        Toast.makeText(this, "Pairing with " + device.getName(), Toast.LENGTH_LONG).show();
//
//        // ペアリングを実行します。
//        try {
//            device.createBond();
//        } catch (Exception e) {
//            Log.e("BluetoothPairingActivity", "Error pairing with device", e);
//        }

        // アクティビティを終了します。
        finish();
    }
}
