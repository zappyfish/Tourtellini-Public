package com.example.tourtellini;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tourtellini.buildings.Building;
import com.example.tourtellini.buildings.BuildingGuide;
import com.example.tourtellini.buildings.BuildingGuideImpl;
import com.example.tourtellini.buildings.BuildingSpatialIndex;
import com.example.tourtellini.phone.pose.PhonePose;
import com.example.tourtellini.phone.pose.PoseManager;
import com.example.tourtellini.phone.pose.PoseUpdateCallback;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback{
    private SurfaceView cameraSurface;
    private SurfaceHolder holder;
    private Camera mCamera;
    private BottomSheetBehavior sheetBehavior;
    private LinearLayout bottom_sheet;

    private TextView addressText;
    private TextView phoneText;
    private TextView buildingText;
    private ProgressBar progressBar;
    private TextView descriptionText;
    private TextView linkText;
    private RatingBar ratingBar;
    private TextView ratingText;
    private TextView reviewsCount;
    private String link; ///link to send users to

    //set gone when in loading state
    private LinearLayout phoneLayout;
    private  LinearLayout addressLayout;
    private LinearLayout ratingLayout;
    private ProgressBar slidebar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        bottom_sheet = findViewById(R.id.bottom_sheet);

        sheetBehavior=BottomSheetBehavior.from(bottom_sheet);
        cameraSurface =(SurfaceView)findViewById(R.id.cameraView);
        holder = cameraSurface.getHolder();
        holder.addCallback(this);

        addressText = bottom_sheet.findViewById(R.id.address);
        phoneText = bottom_sheet.findViewById(R.id.phoneNumber);
       buildingText =bottom_sheet.findViewById(R.id.buildingName);
        progressBar = bottom_sheet.findViewById(R.id.progressBar);
        reviewsCount = bottom_sheet.findViewById(R.id.reviews_count);
        ratingBar = bottom_sheet.findViewById(R.id.ratingBar);
        ratingText =bottom_sheet.findViewById(R.id.rating);
        slidebar = bottom_sheet.findViewById(R.id.progressBar4);

        descriptionText = bottom_sheet.findViewById(R.id.description);
        linkText = bottom_sheet.findViewById(R.id.viewText);
        phoneLayout = bottom_sheet.findViewById(R.id.phoneLayout);
        addressLayout = bottom_sheet.findViewById(R.id.addressLayout);
        ratingLayout = bottom_sheet.findViewById(R.id.ratingLayout);

        switchToLoading(null);
        final BuildingGuide buildingGuide = new BuildingGuideImpl(getApplicationContext());
        // probably want to offer some kind of drop-down to let users set this


        PoseManager.getInstance(getApplicationContext()).addOnPoseUpdateCallback(new PoseUpdateCallback() {
            @Override
            public void onPoseUpdate(PhonePose newPose) {
                BuildingSpatialIndex.LocalPose localPose = buildingGuide.localPose();
                if (localPose == null) {
                    return;
                }

                Optional<Building> focusedBuilding = buildingGuide.getBuildingInFocus();
                if(focusedBuilding.isPresent()){
                    switchToShow(focusedBuilding.get().getName(), focusedBuilding.get().address(), focusedBuilding.get().rating(), focusedBuilding.get().ratingCount(),
                            focusedBuilding.get().placeUrl(), focusedBuilding.get().description(), focusedBuilding.get().phoneNumber());
                }else{
                    switchToLoading(localPose);
                }

            }
        });
    }
    public void onClick(View v){
        if(v.getId() == R.id.nameContainer|| v.getId()==R.id.ratingLayout){
            if(sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }else{
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    private void switchToLoading(BuildingSpatialIndex.LocalPose localPose){
        addressLayout.setVisibility(View.GONE);
        descriptionText.setVisibility(View.GONE);
        linkText.setVisibility(View.GONE);
        phoneLayout.setVisibility(View.GONE);
        ratingLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        slidebar.setVisibility(View.INVISIBLE);

        String bText = "Locating User...";

        if (localPose != null) {
            bText="Scanning Area...";
        }

        buildingText.setText(bText);

    }
    private void switchToShow(String buildingName, String address, Float rating, int ratingCount, String link, String description, String phoneNumber){
        phoneText.setText(phoneNumber);
        String descriText = description;
        if(description.length()>250) {
            descriText = descriText.substring(0, 275) + "...";
        }
        descriptionText.setText(descriText);
        buildingText.setText(buildingName);
        addressText.setText(address);
        ratingText.setText(rating.toString());
        ratingBar.setRating(rating);
        reviewsCount.setText("("+ ratingCount +")");
        this.link = link;

        addressLayout.setVisibility(View.VISIBLE);
        descriptionText.setVisibility(View.VISIBLE);
        ratingLayout.setVisibility(View.VISIBLE);
        slidebar.setVisibility(View.VISIBLE);
        linkText.setVisibility(View.VISIBLE);
        phoneLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCamera = Camera.open(0);
       // mCamera.setPreviewCallback(this);
       Camera.Parameters params = mCamera.getParameters();
       List<String> focusModes = params.getSupportedFocusModes();
       if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
           params.setFocusMode((Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE));
       }
      mCamera.setParameters(params);
       mCamera.setDisplayOrientation(90);


        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
       // mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
         /**
         List<Building> buildings = buildingGuide.getBuildingsInView();
         if(buildings.size()>0) {
         Log.d("BUILDINGS FOUND",buildings.get(0).getName());
         }**/
    }
}
