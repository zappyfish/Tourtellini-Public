package com.example.tourtellini;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import com.example.tourtellini.tours.Tour;
import com.example.tourtellini.tours.TourPreview;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button exploreButton;
    private Button tourButton;
    private final static int PERM_REQUEST_CODE = 110;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tourButton=(Button) findViewById(R.id.tourButton);
        exploreButton =(Button) findViewById(R.id.exploreButton);
        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                    startActivity(intent);
                }else{
                        requestPermission();
                }

            }
        });
        tourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TourListActivity.class);
                startActivity(intent);
            }
        });

        TourPreview.getAvailableTours(getApplicationContext(), new TourPreview.ToursAvailableCallback() {
            @Override
            public void onAvailable(List<TourPreview> tourPreviews) {
                Tour.getTour(getApplicationContext(), tourPreviews.get(0).id(), new Tour.TourCallback() {
                    @Override
                    public void onAvailable(Tour tour) {
                        // tour availble here.
                        Log.e("tour", tour.getName());
                    }
                });
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, PERM_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERM_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(), "In order to explore, you must enable camera and location settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
