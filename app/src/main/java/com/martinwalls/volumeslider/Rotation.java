package com.martinwalls.volumeslider;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import androidx.annotation.Nullable;

public class Rotation implements SensorEventListener {

    private static final String LOG_TAG = Rotation.class.getSimpleName();

    public interface Listener {
        void onRotationChanged(float yaw, float pitch, float roll);
    }

    private static final int SENSOR_DELAY_MICROS = 20 * 1000;

    private final SensorManager sensorManager;

    @Nullable
    private final Sensor gravitySensor;

    private int lastAccuracy;
    private Listener listener;

    Rotation(Activity activity) {
        sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    void startListening(Listener listener) {
        if (this.listener == listener) {
            return;
        }
        this.listener = listener;
        if (gravitySensor == null) {
            Log.e(LOG_TAG, "Rotation sensor not available; no orientation data available.");
            return;
        }
        sensorManager.registerListener(this, gravitySensor, SENSOR_DELAY_MICROS);
    }

    void stopListening() {
        sensorManager.unregisterListener(this);
        listener = null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (lastAccuracy != accuracy) {
            lastAccuracy = accuracy;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (listener == null) {
            return;
        }
        if (lastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (sensorEvent.sensor == gravitySensor) {
            updateRotation(sensorEvent.values);
        }
    }

    private void updateRotation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);

        // convert radians to degrees, approximately
        float yaw = orientation[0] * -57; // left and right turn when upright
        float pitch = orientation[1] * -57; // forward and backward
        float roll = orientation[2] * -57; // left and right roll when upright

        listener.onRotationChanged(yaw, pitch, roll);
    }
}
