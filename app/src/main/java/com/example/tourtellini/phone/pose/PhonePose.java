package com.example.tourtellini.phone.pose;

// TODO: define fields and implement.
public class PhonePose {

    private final float mLat, mLng, mRoll, mPitch, mYaw;

    PhonePose(float lat, float lng, float roll, float pitch, float yaw) {
        mLat = lat;
        mLng = lng;
        mRoll = roll;
        mPitch = pitch;
        mYaw = yaw;
    }

    public float lat() {
        return mLat;
    }

    public float lng() {
        return mLng;
    }

    public float roll() {
        return mRoll;
    }

    public float pitch() {
        return mPitch;
    }

    public float yaw() {
        return mYaw;
    }

}
