package com.example.tourtellini.tours;

import android.content.Context;

import com.android.volley.VolleyError;
import com.example.tourtellini.apis.TourtelliniApiObjectCallback;
import com.example.tourtellini.apis.TourtelliniApiRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tour {

    private String mName;
    private String mDescription;
    private List<TourStop> mTourStops;

    // tourId from TourPreview
    public static void getTour(Context context, int tourId, final TourCallback callback) {
        Map<String, String> args = new HashMap<>();
        args.put("tour_id", String.valueOf(tourId));
        TourtelliniApiRequest.getInstance(context).makeObjectRequest("/tours/load", args, new TourtelliniApiObjectCallback() {
            @Override
            public void onResponse(JSONObject responseData) {
                try {
                    String name = responseData.getString("tour_name");
                    String description = responseData.getString("tour_description");
                    JSONArray stopsJson = responseData.getJSONArray("stops");
                    List<TourStop> stops = new ArrayList<>();
                    for (int i = 0; i < stopsJson.length(); i++) {
                        stops.add(TourStop.fromJSON(stopsJson.getJSONObject(i)));
                    }

                    callback.onAvailable(new Tour(name, description, stops));
                } catch (Exception e) {
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private Tour(String name, String description, List<TourStop> stops) {
        mName = name;
        mDescription = description;
        mTourStops = stops;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public List<TourStop> getTourStops() {
        return mTourStops;
    }

    public static class TourStop {

        private String mAddress;
        private float mLat;
        private float mLng;
        private String mName;
        private String mUserComment;

        static TourStop fromJSON(JSONObject json) {
            try {
                String address = json.getString("address");
                float lat = (float) json.getDouble("lat");
                float lng = (float) json.getDouble("lng");
                String name = json.getString("name");
                String userComment = json.getString("user_comment");
                return new TourStop(address, lat, lng, name, userComment);
            } catch (Exception e) {
                return null;
            }
        }

        private TourStop(String address, float lat, float lng, String name, String userComment) {
            mAddress = address;
            mLat = lat;
            mLng = lng;
            mName = name;
            mUserComment = userComment;
        }

        public float getmLat() {
            return mLat;
        }

        public float getmLng() {
            return mLng;
        }

        public String getmName() {
            return mName;
        }
        public String getmAddress(){
            return mAddress;
        }

        public String getmUserComment() {
            return mUserComment;
        }
    }

    public interface TourCallback {
        void onAvailable(Tour tour);
    }
}
