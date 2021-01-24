package com.example.tourtellini.phone.pose;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.LinkedList;
import java.util.List;

public class PoseManager implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final SensorManager mSensorManager;

    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public final static int FAST_LOCATION_FREQUENCY = 5 * 1000;
    public final static int LOCATION_FREQUENCY = 5 * 1000;

    private final GPSKalmanFilter mKalmanFilter;
    private double mLastLatitude;
    private double mLastLongitude;
    private final double[] mPredicted;

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private final List<PoseUpdateCallback> mPoseUpdateCallbacks;
    private PhonePose mLastPose;
    private static PoseManager sInstance;

    PoseManager(Context context) {
        mLastPose = null;
        mPoseUpdateCallbacks = new LinkedList<>();
        mKalmanFilter = new GPSKalmanFilter(2);
        mPredicted = new double[2];

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

        buildGoogleApiClient(context);
        startLocationUpdates();
    }

    public static PoseManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PoseManager(context.getApplicationContext());
        }

        return sInstance;
    }

    public void addOnPoseUpdateCallback(PoseUpdateCallback callback) {
        mPoseUpdateCallbacks.add(callback);
    }

    public void removeOnPoseUpdateCallback(PoseUpdateCallback callback) {
        mPoseUpdateCallbacks.remove(callback);
    }

    private void updatePose() {
        float[] estimatedLatLng = new float[2];

        // Make sure we have GPS lat, lng.
        if (!mKalmanFilter.getEstimate(estimatedLatLng)) {
            return;
        }

        // TODO: make sure we have an orientation
        final float androidApiYaw = mOrientationAngles[0];
        final float androidApiPitch = mOrientationAngles[1];
        final float androidApiRoll = mOrientationAngles[2];

        // Compute new pose here
        PhonePose newPose = new PhonePose(
                estimatedLatLng[0],
                estimatedLatLng[1],
                androidApiRoll, // (roll) magic
                androidApiPitch, // (pitch) no worry
                androidApiYaw); // (yaw) it worky

        mLastPose = newPose;
        for (PoseUpdateCallback callback : mPoseUpdateCallbacks) {
            callback.onPoseUpdate(newPose);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: something
    }

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
        updatePose();
    }


    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private void updateOrientationAngles() {


        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        // "mRotationMatrix" now has up-to-date information.

        float[] rotationMatRemapped = new float[9];
        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatRemapped);
        System.arraycopy(rotationMatRemapped, 0, mRotationMatrix, 0, mRotationMatrix.length);

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        // "mOrientationAngles" now has up-to-date information.
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

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        mLastLatitude = location.getLatitude();
        mLastLongitude = location.getLongitude();

        // Add the measurement.
        mKalmanFilter.addMeasurement(location);
        updatePose();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    ///////////// 1

    /**
     * builds a GoogleApiClient
     */
    private synchronized void buildGoogleApiClient(Context context) {
        // setup googleapi client
        mGoogleApiClient = new GoogleApiClient.Builder(context)
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
        // TODO: request permissions properly.
        if (ActivityCompat.checkSelfPermission(mGoogleApiClient.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mGoogleApiClient.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        );
    }

}
