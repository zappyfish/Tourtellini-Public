package com.example.tourtellini.apis;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class TourtelliniApiRequest {

    private static final String API_BASE_URL = "https://arcane-atoll-68110.herokuapp.com";

    private RequestQueue mRequestQueue;

    private static TourtelliniApiRequest sInstance;

    private TourtelliniApiRequest(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    private static String buildRequestUrl(String endpointPath, Map<String, String> args) {
        if (args.isEmpty()) {
            return API_BASE_URL + endpointPath;
        }

        // Crappy string building
        char prependChar = '?';
        StringBuilder requestUrl = new StringBuilder(API_BASE_URL);
        requestUrl.append(endpointPath);
        for (String key : args.keySet()) {
            requestUrl.append(prependChar).append(key).append('=').append(args.get(key));
            prependChar = '&';
        }

        return requestUrl.toString();
    }

    public static TourtelliniApiRequest getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TourtelliniApiRequest(context);
        }

        return sInstance;
    }

                                             // For the sake of simplicity, all requests are GET.
    public void makeRequest(String endpointPath,
                            Map<String, String> args,
                            final TourtelliniApiCallback callback) {
        final String requestUrl = buildRequestUrl(endpointPath, args);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        callback.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onErrorResponse(error);
                    }
                }
            );

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 10;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        mRequestQueue.add(jsonArrayRequest);
    }

    public void makeObjectRequest(String endpointPath,
                            Map<String, String> args,
                            final TourtelliniApiObjectCallback callback) {
        final String requestUrl = buildRequestUrl(endpointPath, args);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onErrorResponse(error);
                    }
                }
        );

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 10;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        mRequestQueue.add(jsonArrayRequest);
    }
}
