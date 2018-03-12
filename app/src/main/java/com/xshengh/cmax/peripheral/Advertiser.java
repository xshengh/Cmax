package com.xshengh.cmax.peripheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by xshengh on 18/3/4.
 */

public class Advertiser {
    private static final Advertiser sAdvertiser = new Advertiser();
    private static final String PREFIX = "PBS";
    private static final List<BlackoutDate> DATE_LIST = new ArrayList<>();
    private static final List<BlackoutTime> TIME_LIST = new ArrayList<>();
    private static final List<String> ADV_DATA = new ArrayList<>(9);
    private volatile boolean mLoop;
    private volatile int mCurPos = 0;
    private Handler mHandler;
    private BluetoothLeAdvertiser mAdvertiser;
    private AdvertiseSettings mSettings;
    private AdvertiseCallback mAdvertisingCallback;
    private AdvertiseData mAdvertiseData;

    static {
        for (int i = 0; i <= 7; i++) {
            BlackoutTime time = new BlackoutTime();
            time.start.add(Calendar.HOUR_OF_DAY, i);
            time.end.add(Calendar.HOUR_OF_DAY, i);
            TIME_LIST.add(time);
            BlackoutDate date = new BlackoutDate();
            date.start.add(Calendar.MONTH, i + 2);
            date.end.add(Calendar.MONTH, i + 2);
            DATE_LIST.add(date);
        }
        prepareAdvData();
    }

    private static void prepareAdvData() {
//        ADV_DATA.add("PBSo71FF0830000000000000");
//        ADV_DATA.add("PBSo72110110110000000000");
//        ADV_DATA.add("PBSo73191514121530000000");
//        ADV_DATA.add("PBSo7410:PU?10;0V?10<PV?");
//        ADV_DATA.add("PBSo7510=0W?10>PW?10?0X?");
//        ADV_DATA.add("PBSo7610@PX?10A0Y?000000");
//        ADV_DATA.add("PBSo771Q0X20111X401Q1X60");
//        ADV_DATA.add("PBSo78112X801Q2X:0113X<0");
//        ADV_DATA.add("PBSo791Q3X>0114X@0000000");
        ADV_DATA.add(createDateTimePacket());
        ADV_DATA.add(createDeviceIdPacket());
        ADV_DATA.add(createDeviceSettingPacket());
        for (int i = 0; i < 3; i++) {
            ADV_DATA.add(createBlackOutTimePacket(i));
        }
        for (int i = 0; i < 3; i++) {
            ADV_DATA.add(createBlackOutDatePacket(i));
        }
    }

    private static String createDateTimePacket() {
        StringBuilder sb = createInitedBuilder();
        sb.append(encodeAscii(1, 1));
        Calendar cal = Calendar.getInstance();
        System.out.println("------ packet 1 : " + cal);
        sb.append(encodeAscii(cal.get(Calendar.SECOND), 1));
        sb.append(encodeAscii(cal.get(Calendar.MINUTE), 1));
        sb.append(encodeAscii(cal.get(Calendar.HOUR_OF_DAY), 1));
        sb.append(encodeAscii(cal.get(Calendar.DAY_OF_MONTH), 1));
        sb.append(encodeAscii(cal.get(Calendar.MONTH), 1));
        sb.append(encodeAscii(cal.get(Calendar.YEAR) - 2018, 1));
        sb.append(encodeAscii(0, 12));
        return sb.toString();
    }

    private static String createDeviceIdPacket() {
        StringBuilder sb = createInitedBuilder();
        sb.append(encodeAscii(2, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(1, 2));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(1, 2));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(1, 2));
        sb.append(encodeAscii(0, 9));
        return sb.toString();
    }

    private static String createDeviceSettingPacket() {
        StringBuilder sb = createInitedBuilder();
        sb.append(encodeAscii(3, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(9, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(5, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(4, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(2, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(-59 & 0x00FF, 2));
        sb.append(encodeAscii(0, 7));
        return sb.toString();
    }

    private static String createBlackOutTimePacket(int index) {
        StringBuilder sb = createInitedBuilder();
        sb.append(encodeAscii(4 + index, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(encodeTimeSet(TIME_LIST.get(3 * index)), 5));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(encodeTimeSet(TIME_LIST.get(3 * index + 1)), 5));
        if (index < 2) {
            sb.append(encodeAscii(1, 1)).append(encodeAscii(encodeTimeSet(TIME_LIST.get(3 * index + 2)), 5));
        } else {
            sb.append(encodeAscii(0, 6));
        }
        return sb.toString();
    }

    private static String createBlackOutDatePacket(int index) {
        StringBuilder sb = createInitedBuilder();
        sb.append(encodeAscii(7 + index, 1));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(encodeDateSet(DATE_LIST.get(3 * index)), 5));
        sb.append(encodeAscii(1, 1)).append(encodeAscii(encodeDateSet(DATE_LIST.get(3 * index + 1)), 5));
        if (index < 2) {
            sb.append(encodeAscii(1, 1)).append(encodeAscii(encodeDateSet(DATE_LIST.get(3 * index + 2)), 5));
        } else {
            sb.append(encodeAscii(0, 6));
        }
        return sb.toString();
    }

    private static int encodeDateSet(BlackoutDate date) {
        int encodeStartDate = (date.start.get(Calendar.DATE) & 0x1F) + ((date.start.get(Calendar.MONTH) & 0xF) << 5) + (((date.start.get(Calendar.YEAR) - 2018) & 0x1F) << 9);
        System.out.println("start date : " + date.start.get(Calendar.DATE) + ", " + date.start.get(Calendar.MONTH) + ", " + date.start.get(Calendar.YEAR) + ", " + encodeStartDate);
        int encodeEndDate = (date.end.get(Calendar.DATE) & 0x1F) + ((date.end.get(Calendar.MONTH) & 0xF) << 5) + (((date.end.get(Calendar.YEAR) - 2018) & 0x1F) << 9);
        System.out.println("end date : " + date.end.get(Calendar.DATE) + ", " + date.end.get(Calendar.MONTH) + ", " + date.end.get(Calendar.YEAR) + ", " + encodeEndDate);
        int encodeOutputDate = encodeStartDate + (encodeEndDate << 14);
        if (date.enabled) {
            encodeOutputDate |= 0x20000000;
        }
        System.out.println("encodeOutputDate : " + encodeOutputDate);
        return encodeOutputDate;
    }

    private static int encodeTimeSet(BlackoutTime time) {
        int encodeStartTime = (time.start.get(Calendar.MINUTE) & 0x3F) + ((time.start.get(Calendar.HOUR_OF_DAY) & 0x1F) << 6);
        System.out.println("start minute : " + time.start.get(Calendar.MINUTE) + ", hour : " + time.start.get(Calendar.HOUR_OF_DAY) + ", " + encodeStartTime);
        int encodeEndTime = ((time.end.get(Calendar.MINUTE) & 0x3F) + ((time.end.get(Calendar.HOUR_OF_DAY) & 0x1F) << 6)) << 11;
        System.out.println("end minute : " + time.end.get(Calendar.MINUTE) + ", hour : " + time.end.get(Calendar.HOUR_OF_DAY) + ", " + encodeEndTime);
        int encodeWeekday = time.weekday << 22;
        System.out.println("encodeWeekday : " + encodeWeekday);


        int encodeOutputTime = encodeStartTime + encodeEndTime + encodeWeekday;

        if (time.enabled) {
            encodeOutputTime |= 0x20000000;
        }
        System.out.println("encodeOutputTime : " + encodeOutputTime);

        return encodeOutputTime;
    }

    private static StringBuilder createInitedBuilder() {
        StringBuilder sb = new StringBuilder(PREFIX);
        sb.append(encodeAscii(0x1FF, 2));
        return sb;
    }

    public static String encodeAscii(int number, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) ((number & 0x3F) + 0x30));
            number >>= 6;
        }
        return sb.toString();
    }

    public static Advertiser getInstance() {
        return sAdvertiser;
    }

    private Advertiser() {
        HandlerThread thread = new HandlerThread("Advertiser");
        thread.start();
        mHandler = new Handler(thread.getLooper());
        initAdvertiser();
    }

    private void initAdvertiser() {
        mAdvertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        mSettings = new AdvertiseSettings.Builder().build();
        mAdvertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.i("BLE", "LE Advertise success.");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE", "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };
        mAdvertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .build();
    }

    public void start() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLoop = true;
                while (mLoop) {
                    int pos = mCurPos % ADV_DATA.size();
                    mCurPos++;
                    System.out.println("----- advertisement : " + pos);
                    advertiseData(ADV_DATA.get(pos));
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mCurPos = 0;
            }
        });
    }

    public void stop() {
        mLoop = false;
    }

    private void advertiseData(String d) {
        BluetoothAdapter.getDefaultAdapter().setName(d);
        mAdvertiser.stopAdvertising(mAdvertisingCallback);
        System.out.println("-------- data : " + d);
        Log.i("BLE", "before callback");
        mAdvertiser.startAdvertising(mSettings, mAdvertiseData, mAdvertisingCallback);
        Log.i("BLE", "start advertising");
    }
}
