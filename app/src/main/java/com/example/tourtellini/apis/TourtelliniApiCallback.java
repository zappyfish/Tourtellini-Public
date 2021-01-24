package com.example.tourtellini.apis;

import com.android.volley.VolleyError;

import org.json.JSONArray;

public interface TourtelliniApiCallback {

    // A callback to be invoked upon a successful API response.
    void onResponse(JSONArray responseData);

    // Callback to be invoked upon failure.
    public void onErrorResponse(VolleyError error);
}
