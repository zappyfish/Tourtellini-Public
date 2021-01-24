package com.example.tourtellini;


import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tourtellini.tours.Tour;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class TourActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private TextView title;
    private TextView description;
    private TextView address;
    private Button nextButton;
    private Button cancelButton;
    private int tourId;


    private int counter = 0;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tours_layout);
        title = findViewById(R.id.toursTitle);
        description = findViewById(R.id.toursDescription);
        address = findViewById(R.id.toursAddress);
        address.setVisibility(View.GONE);
        nextButton =findViewById(R.id.nextButton);
        cancelButton=findViewById(R.id.leaveButton);

        cancelButton.setOnClickListener(new  View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TourActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        tourId = getIntent().getIntExtra("id", -1);
        if(tourId==-1){
            Toast.makeText(getApplicationContext(), "Unable to start. Check your Internet Connection", Toast.LENGTH_SHORT);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return;
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.streetview);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
       mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_dark));
        // Add a marker in Sydney and move the camera


        Tour.getTour(getApplicationContext(), tourId, new Tour.TourCallback() {
            @Override
            public void onAvailable(final Tour tour) {

                title.setText(tour.getName());
                description.setText(tour.getDescription());
                final List<Tour.TourStop> tourStops  = tour.getTourStops();
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(counter == tourStops.size()){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            return;
                        }
                        nextButton.setText("Next");
                        title.setText(tourStops.get(counter).getmName());
                        address.setVisibility(View.VISIBLE);
                        address.setText(tourStops.get(counter).getmAddress());
                        description.setText(tourStops.get(counter).getmUserComment());
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(tourStops.get(counter).getmLat(), tourStops.get(counter).getmLng()), 16);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(tourStops.get(counter).getmLat(), tourStops.get(counter).getmLng())).title(tourStops.get(counter).getmName()));
                        mMap.animateCamera(update);

                        counter++;
                        if(counter==tourStops.size()){
                            nextButton.setText("Finish");
                        }
                    }
                });
            }
        });
    }
}
