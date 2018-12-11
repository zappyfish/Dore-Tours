package com.example.liamkelly.doretours.data.location;

import android.content.Context;

import com.example.liamkelly.doretours.data.pose.PoseManager;

public class NavigationSession {

    private final HardcodedBuilding mDestination;
    private final double[] mDirectionVector;

    private double mLastLatitude;
    private double mLastLongitude;
    private double mLastNorthMagnitude;
    private double mLastEastMagnitude;
    private double mLastZAngle;

    private final PoseManager.PoseCallback mPoseCallback = new PoseManager.PoseCallback() {
        @Override
        public void callback(double northMagnitude, double eastMagnitude, double zAngle) {
            mLastNorthMagnitude = northMagnitude;
            mLastEastMagnitude = eastMagnitude;
            mLastZAngle = zAngle;
            computeDirectionVector();
        }

        @Override
        public void callback(float roll, float pitch, float yaw) {

        }
    };

    private final GPSManager.Callback mGPSCallback = new GPSManager.Callback() {
        @Override
        public void onData(double latitude, double longitude) {
            if (isFinishedNavigating(latitude, longitude)) {
                finishNavigation();
            } else {
                mLastLatitude = latitude;
                mLastLongitude = longitude;
                computeDirectionVector();
            }
        }
    };

    public NavigationSession(Context context, HardcodedBuilding building) {
        mDestination = building;
        mDirectionVector = new double[2];
        GPSManager.getInstance(context).addCallback(mGPSCallback);
        PoseManager.getInstance(context).addCallback(mPoseCallback);
    }

    private void finishNavigation() {
        GPSManager.getInstance(null).removeCallback(mGPSCallback);
        PoseManager.getInstance(null).removeCallback(mPoseCallback);
    }

    private boolean isFinishedNavigating(double latitude, double longitude) {
        return mDestination.inBuilding(latitude, longitude);
    }

    private void computeDirectionVector() {
        double destRelativeNorth = mDestination.getLatitude() - mLastLatitude;
        double destRelativeEast = mDestination.getLongitude() - mLastLongitude;

        double normalizationFactor = 1.0 / (Math.sqrt(Math.pow(destRelativeEast, 2) + Math.pow(destRelativeNorth, 2)));

        destRelativeNorth *= normalizationFactor;
        destRelativeEast *= normalizationFactor;

        double mEastDifference = destRelativeEast;
        double mNorthDifference = destRelativeNorth;

        // Now rotate into the frame of reference of the viewer
        double theta = computeRotationAngle(mLastEastMagnitude, mLastNorthMagnitude);
        // R^2 rotation matrix
        mDirectionVector[0] = mEastDifference * Math.cos(theta) - mNorthDifference * Math.sin(theta);
        mDirectionVector[1] = mEastDifference * Math.sin(theta) + mNorthDifference * Math.cos(theta);
    }

    private double computeRotationAngle(double x, double y) {
        if (y == 0) {
            return Math.PI / 2;
        } else {
            return Math.atan(x / y);
        }
    }

    public double[] getDirectionVector() {
        return mDirectionVector;
    }
}
