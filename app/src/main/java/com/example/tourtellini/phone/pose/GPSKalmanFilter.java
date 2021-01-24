package com.example.tourtellini.phone.pose;

import android.location.Location;

public class GPSKalmanFilter {

    private final float[] mVariable;
    private final float[] mMeasurement;
    private boolean mShouldPredict = false;
    private long mLastTimeNanoseconds;
    private double mVariance;
    private final static double PROCCESS_ERROR_COVARIANCE = 3;
    private final static double GPS_ACCURACY = 0.0001; // In lat/lon

    public GPSKalmanFilter(int vectorSize) {
        mVariable = new float[vectorSize];
        mMeasurement = new float[vectorSize];
        mVariance = -1;
    }

    // Returns true if data was written to output (i.e. all measurements have been made)
    synchronized void addMeasurement(Location location) {
        mMeasurement[0] = (float) location.getLatitude();
        mMeasurement[1] = (float) location.getLongitude();
        if (!mShouldPredict) {
            mShouldPredict = true;
            mLastTimeNanoseconds = System.nanoTime();
            getEstimate(mVariable);
            mVariance = GPS_ACCURACY * GPS_ACCURACY;
        } else { // Only predict after we have all measurements
            performUpdate(location.getAccuracy());
        }
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

    boolean getEstimate(float[] output) {
        if (!mShouldPredict) {
            return false;
        }

        for (int i = 0; i < mMeasurement.length; i++) {
            output[i] = mMeasurement[i];
        }

        return true;
    }
}