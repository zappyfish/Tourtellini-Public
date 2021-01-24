package com.example.tourtellini.buildings;

import android.content.Context;

import com.example.tourtellini.phone.pose.PoseManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BuildingGuideImpl implements BuildingGuide {

    private BuildingCache mBuildingCache;
    private final Context mContext;
    private static List<String> mCategories = new ArrayList<String>(Arrays.asList(
            "advertising-marketing",
            "agriculture",
            "arts-entertainment",
            "automotive",
            "city-infrastructure",
            "commercial-industrial",
            "education",
            "finance",
            "food-beverage",
            "government-building",
            "hotel-lodging",
            "legal",
            "local-services",
            "locality",
            "media-news-company",
            "medical-health",
            "outdoor-places",
            "public-services-government",
            "real-estate",
            "religion",
            "residence-other",
            "science-engineering",
            "shopping-retail",
            "spa-beauty-personal-care",
            "sports-recreation",
            "travel-transportation")
    );

    private void setDefaultBuildingType() {
        final String allCategoriesString = String.join(",", mCategories);
        setBuildingType(BuildingType.CATEGORY, allCategoriesString);
    }

    public BuildingGuideImpl(Context context) {
        mContext = context;

        // Set all categories as a default.
        setDefaultBuildingType();
    }

    public static List<String> allCategories() {
         return mCategories;
    }

    @Override
    public void setBuildingType(BuildingType typeKey, String typeVal) {
        if (mBuildingCache != null) {
            PoseManager.getInstance(mContext).removeOnPoseUpdateCallback(mBuildingCache);
        }

        mBuildingCache = new BuildingCache(mContext, typeKey.asString(), typeVal);

        // Let the new building cache receive pose callbacks.
        PoseManager.getInstance(mContext).addOnPoseUpdateCallback(mBuildingCache);
    }

    @Override
    public List<Building> getBuildingsInView() {
        if (mBuildingCache == null) {
            return new LinkedList<>();
        }

        return mBuildingCache.buildingsInView();
    }

    @Override
    public Optional<Building> getBuildingInFocus() {
        if (mBuildingCache == null) {
            return Optional.empty();
        }

        return mBuildingCache.buildingInFocus();
    }

    @Override
    public BuildingSpatialIndex.LocalPose localPose() {
        return mBuildingCache.lastPoseLocal();
    }
}
