package com.xshengh.cmax;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;

import com.xshengh.cmax.peripheral.Advertiser;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.adv_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Advertiser.getInstance().start();
            }
        });
        findViewById(R.id.adv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Advertiser.getInstance().stop();
            }
        });
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {
            adapter.enable();
        }
    }
}
