package com.example.tourtellini.buildings;

import android.graphics.Point;

import com.example.tourtellini.phone.pose.PhonePose;
import com.example.tourtellini.phone.screen.ScreenInfo;

import org.json.JSONArray;
import org.json.JSONObject;

// TODO: implement me
public class Building {

    private final String mName;
    private final String mId;

    private final float mLat;
    private final float mLng;

    private final String mAddress;
    private final float mRating;
    private final int mRatingCount;
    private final String mDescription;
    private final String mPhoneNumber;
    private final String mPlaceUrl;

    static Building fromJson(JSONObject jsonObject) {
        try {
            String name = jsonObject.getString("name");
            String id = jsonObject.getString(("_id"));
            JSONArray coords = jsonObject.getJSONObject("location").getJSONArray("coordinates");
            float lng = (float)coords.getDouble(0);
            float lat = (float)coords.getDouble(1);

            if (jsonObject.has("yelp_review")) {
                JSONObject yelpJSON = jsonObject.getJSONObject("yelp_review");
                String address = yelpJSON.getString("address");
                float rating = (float) yelpJSON.getDouble("rating");
                int ratingCount = yelpJSON.getInt("review_count");
                String description = yelpJSON.getString("description");
                String phoneNumber = yelpJSON.getString("phone_number");
                // TODO: add this to backend
                String placeUrl = "link.com";
                return new Building(name, id, lat, lng, address, rating, ratingCount,
                        description, phoneNumber, placeUrl);
            } else {
                return new Building(name, id, lat, lng);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Building(String name, String id, float lat, float lng) {
        mName = name;
        mId = id;
        mLat = lat;
        mLng = lng;

        mAddress = "N/A";
        mRating = 0;
        mRatingCount = 0;
        mDescription = "N/A";
        mPhoneNumber = "N/A";
        mPlaceUrl = "N/A";
    }

    private Building(String name, String id, float lat, float lng, String address, float rating,
                     int ratingCount, String description, String phoneNumber, String placeUrl) {
        mName = name;
        mId = id;
        mLat = lat;
        mLng = lng;
        mAddress = address;
        mRating = rating;
        mRatingCount = ratingCount;
        mDescription = description;
        mPhoneNumber = phoneNumber;
        mPlaceUrl = placeUrl;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public float lat() {
        return mLat;
    }

    public float lng() {
        return mLng;
    }

    public String address() {
        return mAddress;
    }

    public float rating() {
        return mRating;
    }

    public int ratingCount() {
        return mRatingCount;
    }

    public String description() {
        return mDescription;
    }

    public String phoneNumber() {
        return mDescription;
    }

    public String placeUrl() {
        return mPlaceUrl;
    }
}
