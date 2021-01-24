package com.example.tourtellini.tours;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.tourtellini.apis.TourtelliniApiCallback;
import com.example.tourtellini.apis.TourtelliniApiRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TourPreview {

    private int mId;
    private String mName;
    private String mDescription;

    private TourPreview(String description, String name, int id) {
        mId = id;
        mDescription = description;
        mName = name;
    }

    public int id() {
        return mId;
    }

    public String name() {
        return mName;
    }

    public String description() {
        return mDescription;
    }

    private static TourPreview fromJSONObject(JSONObject jsonObject) {
        try {
            String description = jsonObject.getString("description");
            String name = jsonObject.getString("name");
            int id = jsonObject.getInt("id");
            return new TourPreview(description, name, id);
        } catch (Exception e) {
            return null;
        }
    }

    public static void getAvailableTours(Context context, final ToursAvailableCallback callback) {

        Map<String, String> args = new HashMap<>();
        TourtelliniApiRequest.getInstance(context).makeRequest("/tours/available", args,
                new TourtelliniApiCallback() {
                    @Override
                    public void onResponse(JSONArray responseData) {
                        List<TourPreview> previews = new LinkedList<>();

                        try {
                            for (int i = 0; i < responseData.length(); i++) {
                                previews.add(fromJSONObject(responseData.getJSONObject(i)));
                            }

                            callback.onAvailable(previews);
                        } catch (Exception e) {
                            return;
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });


    }

    public interface ToursAvailableCallback {

        void onAvailable(List<TourPreview> tourPreviews);
    }
}
