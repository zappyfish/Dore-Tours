package com.example.liamkelly.doretours.data.pose;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.LinkedList;
import java.util.List;

public class PoseManager implements SensorEventListener {

    private final List<PoseCallback> mCallbacks;
    private final SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private final double[] mOrientationVector = new double[3];

    private static PoseManager sInstance;

    private PoseManager(Context context) {
        mCallbacks = new LinkedList<>();
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public static synchronized PoseManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PoseManager(context);
        }
        return sInstance;
    }

    public void addCallback(PoseCallback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }
        updateOrientationAngles();
        for (PoseCallback callback : mCallbacks) {
            callback.callback(mOrientationAngles[0], mOrientationAngles[2], mOrientationAngles[1]);
            callback.callback(mOrientationVector[0], mOrientationVector[1], mOrientationVector[2]);
        }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        computeOrientationVector();
        // "mOrientationAngles" now has up-to-date information.
    }

    private void computeOrientationVector() {
        float x = mOrientationAngles[0];
        float y = mOrientationAngles[2];
        float z = mOrientationAngles[1];
        mOrientationVector[0] = -mRotationMatrix[5];
        mOrientationVector[1] = -mRotationMatrix[2];
        mOrientationVector[2] = 90 -mRotationMatrix[8] * 90;
    }

    public interface PoseCallback {

        void callback(double northMagnitude, double eastMagnitude, double zAngle);

        void callback(float roll, float pitch, float yaw);
    }
}
