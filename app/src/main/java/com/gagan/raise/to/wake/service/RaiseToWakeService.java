package com.gagan.raise.to.wake.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gagan.raise.to.wake.persistant.AppPref;

/**
 * Service to wake device when user raised it.
 * Created by gaganpreetsingh on 2/27/2017.
 */
public class RaiseToWakeService extends Service
        implements
        SensorEventListener {

    private static final String TAG = "RaiseToWakeService";

    public static boolean mIsServiceRunning;

    private boolean mIsRaiseToWakeFired;

    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private Sensor mAccelerationSensor;

    private PowerManager.WakeLock mWakeLock;
    private AppPref mPref;

    private boolean mIsProximitySensorCovered;

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = AppPref.getInstance(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // register broadcast receiver
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenEventReceiver, screenStateFilter);

        startForeground(1, new Notification());
        mIsServiceRunning = true;

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PROXIMITY:
                mIsProximitySensorCovered = event.values[0] != 100;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                // if proximity sensor is covered, or raise to wake
                // already invoked, don't do anything.
                if(mIsRaiseToWakeFired) return;

                if(mPref.isProximitySensorEnabled() && mIsProximitySensorCovered) {
                    Log.d(TAG, "Proximity sensor is enabled and covered!");
                    return;
                }

                // check for values
                if (event.values[1] >= 8 && event.values[2] <= 6) {
                    mIsRaiseToWakeFired = true;
                    Log.d("Event", "Raise to wake event fired!");
                    try {
                        mWakeLock.acquire();
                    } finally {
                        mWakeLock.release();
                    }
                }

                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenEventReceiver);
        mIsServiceRunning = false;
    }

    /**
     * Screen event receiver to register/un-register sensors
     * when screen goes off/on.
     */
    private BroadcastReceiver mScreenEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsRaiseToWakeFired = false;
            Log.d(TAG, "Action: " + intent.getAction());

            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_OFF:
                    mSensorManager.registerListener(RaiseToWakeService.this, mProximitySensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    mSensorManager.registerListener(RaiseToWakeService.this, mAccelerationSensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                    break;

                case Intent.ACTION_SCREEN_ON:
                    mSensorManager.unregisterListener(RaiseToWakeService.this, mProximitySensor);
                    mSensorManager.unregisterListener(RaiseToWakeService.this, mAccelerationSensor);
                    break;
            }
        }
    };
}
