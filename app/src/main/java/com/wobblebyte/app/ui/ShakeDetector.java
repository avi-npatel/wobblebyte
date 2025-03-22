package com.wobblebyte.app.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

/** Calls back when the phone is shaken hard enough, using the accelerometer. */
public final class ShakeDetector implements SensorEventListener {

    public interface Listener {
        void onShake();
    }

    /** Force in g (1 g is resting gravity) that counts as a shake. */
    private static final float THRESHOLD_G = 2.7f;
    private static final long COOLDOWN_MS = 900;

    private final SensorManager sensors;
    private final Sensor accelerometer;
    private final Listener listener;
    private long lastShake;

    public ShakeDetector(Context context, Listener listener) {
        this.sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensors == null ? null : sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.listener = listener;
    }

    public void start() {
        if (accelerometer != null) {
            sensors.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (sensors != null) {
            sensors.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gx = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gy = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gz = event.values[2] / SensorManager.GRAVITY_EARTH;
        double force = Math.sqrt(gx * gx + gy * gy + gz * gz);

        if (force > THRESHOLD_G) {
            long now = SystemClock.elapsedRealtime();
            if (now - lastShake > COOLDOWN_MS) {
                lastShake = now;
                listener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed.
    }
}
