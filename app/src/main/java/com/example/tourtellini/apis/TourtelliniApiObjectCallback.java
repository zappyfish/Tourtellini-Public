package com.example.tourtellini.apis;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface TourtelliniApiObjectCallback {

    // A callback to be invoked upon a successful API response.
    void onResponse(JSONObject responseData);

    // Callback to be invoked upon failure.
    public void onErrorResponse(VolleyError error);
}
