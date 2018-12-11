package com.example.liamkelly.doretours.data.location;

import android.location.Location;

public class GPSKalmanFilter {

    private final double[] mVariable;
    private final double[] mMeasurement;
    private final double[] mPrediction;
    private boolean mShouldPredict = false;
    private long mLastTimeNanoseconds;
    private double mVariance;
    private final static double PROCCESS_ERROR_COVARIANCE = 3;
    private final static double GPS_ACCURACY = 0.0001; // In lat/lon

    public GPSKalmanFilter(int vectorSize) {
        mVariable = new double[vectorSize];
        mMeasurement = new double[vectorSize];
        mPrediction = new double[vectorSize];
        mVariance = -1;
    }

    // Returns true if data was written to output (i.e. all measurements have been made)
    public synchronized boolean addMeasurement(Location location, double[] output) {
        mMeasurement[0] = location.getLatitude();
        mMeasurement[1] = location.getLongitude();
        if (!mShouldPredict) {
            mShouldPredict = true;
            mLastTimeNanoseconds = System.nanoTime();
            getEstimate(mMeasurement, mVariable);
            getEstimate(mMeasurement, output);
            mVariance = GPS_ACCURACY * GPS_ACCURACY;
        } else if (mShouldPredict) { // Only predict after we have all measurements
            performUpdate(location.getAccuracy());
            getEstimate(mVariable, output);
        }
        return mShouldPredict;
    }

    private void performUpdate(double accuracy) {
        long currentTime = System.nanoTime();
        long deltaTime = currentTime - mLastTimeNanoseconds;
        mLastTimeNanoseconds = currentTime;
        mVariance += PROCCESS_ERROR_COVARIANCE * PROCCESS_ERROR_COVARIANCE * deltaTime / 1000000000.0;
        double kalmanGain = mVariance / (mVariance + (accuracy * accuracy)); // Kalman gain
        for (int i = 0; i < mMeasurement.length; i++) {
            mVariable[i] += kalmanGain * (mMeasurement[i] - mVariable[i]);
        }
        mVariance = (1 - kalmanGain) * mVariance;
    }

    private void getEstimate(double[] input, double[] output) {
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
    }
}
