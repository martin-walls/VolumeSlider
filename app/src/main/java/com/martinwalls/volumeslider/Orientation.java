package com.martinwalls.volumeslider;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class Orientation implements SensorEventListener {

    private static final String LOG_TAG = Orientation.class.getSimpleName();

    public interface Listener {
        void onOrientationChanged(float yaw);
    }

    private static final int SENSOR_DELAY_MICROS = 16 * 1000;

    private final WindowManager windowManager;
    private final SensorManager sensorManager;

    @Nullable
    private final Sensor gravitySensor;

    private int lastAccuracy;
    private Listener listener;

    public Orientation(Activity activity) {
        windowManager = activity.getWindowManager();
        sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    public void startListening(Listener listener) {
        if (this.listener == listener) {
            return;
        }
        this.listener = listener;
        if (gravitySensor == null) {
            Log.e(LOG_TAG, "Gravity sensor not available; no orientation data available.");
            return;
        }
        sensorManager.registerListener(this, gravitySensor, SENSOR_DELAY_MICROS);
    }

    public void stopListening() {
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
            updateOrientation(sensorEvent.values);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix);

        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        // convert radians to degrees
        float yaw = orientation[0] * -57;
//        float pitch = orientation[1] * -57;
//        float roll = orientation[2] * -57;

        listener.onOrientationChanged(yaw);
    }
}
