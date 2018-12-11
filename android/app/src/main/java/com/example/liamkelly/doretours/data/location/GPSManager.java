package com.example.liamkelly.doretours.data.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GPSManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /***********************************************************************************************
     * properties
     **********************************************************************************************/
    private static GPSManager mInstance = null;
    private Context mContext;
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public final static int FAST_LOCATION_FREQUENCY = 5 * 1000;
    public final static int LOCATION_FREQUENCY = 5 * 1000;
    private final List<Callback> mCallbacks;
    private double mLastLatitude;
    private double mLastLongitude;
    private final double[] mPredicted;
    private final GPSKalmanFilter mKalmanFilter;

    /***********************************************************************************************
     * methods
     **********************************************************************************************/
    /**
     * constructor
     */
    public GPSManager(Context context) {
        mContext = context;
        mCallbacks = new ArrayList<>();
        buildGoogleApiClient();
        mKalmanFilter = new GPSKalmanFilter(2);
        mPredicted = new double[2];
    }

    /**
     * destructor
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopLocationUpdates();
    }

    public static GPSManager getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new GPSManager(context);
        }
        return mInstance;
    }

    public double getLastLatitude() {
        return mLastLatitude;
    }

    public double getLastLongitude() {
        return mLastLongitude;
    }

    ///////////// 1
    /**
     * builds a GoogleApiClient
     */
    private synchronized void buildGoogleApiClient() {
        // setup googleapi client
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // setup location updates
        configRequestLocationUpdate();
    }

    ///////////// 2
    /**
     * config request location update
     */
    private void configRequestLocationUpdate() {
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_FREQUENCY)
                .setFastestInterval(FAST_LOCATION_FREQUENCY);
    }

    ///////////// 3
    /**
     * request location updates
     */
    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        );
    }

    /**
     * start location updates
     */
    public void startLocationUpdates() {
        // connect and force the updates
        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    /**
     * removes location updates from the FusedLocationApi
     */
    public void stopLocationUpdates() {
        // stop updates, disconnect from google api
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    /**
     * get last available location
     * @return last known location
     */
    public Location getLastLocation() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            // return last location
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        else {
            startLocationUpdates(); // start the updates
            return null;
        }
    }

    /***********************************************************************************************
     * GoogleApiClient Callbacks
     **********************************************************************************************/
    @Override
    public void onConnected(Bundle bundle) {
        // do location updates
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // connection to Google Play services was lost for some reason
        if (null != mGoogleApiClient) {
            mGoogleApiClient.connect(); // attempt to establish a new connection
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /***********************************************************************************************
     * Location Listener Callback
     **********************************************************************************************/
    @Override
    public void onLocationChanged(Location location) {
        mLastLatitude = location.getLatitude();
        mLastLongitude = location.getLongitude();
        mKalmanFilter.addMeasurement(location, mPredicted);
        if (location != null) {
            // send location in broadcast
            for (Callback callback : mCallbacks) {
                callback.onData(mPredicted[0], mPredicted[1]);
            }
        }
    }

    public void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public static interface Callback {

        void onData(double latitude, double longitude);
    }

}