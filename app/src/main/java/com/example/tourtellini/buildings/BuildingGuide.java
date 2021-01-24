package com.example.tourtellini.buildings;

import java.util.List;
import java.util.Optional;

public interface BuildingGuide {

    enum BuildingType {
        CATEGORY("categories"), // e.g. "food-beverage" (see radar.io docs)
        GROUP("groups"), // e.g. see radar.io docs
        CHAIN("chains");  // e.g. "starbucks"

        private final String mCacheKeyType;

        BuildingType(String keyType) {
            mCacheKeyType = keyType;
        }

        String asString() {
            return mCacheKeyType;
        }
    }

    /**
     * This is kinda poorly named, but basically it's used to set what kind of buildings the
     * building guide will give you. For example, if you call:
     *
     * setBuildingType(BuildingType.CATEGORY, "food-beverage")
     *
     * then you'll get only get restaurants nearby. If you call:
     *
     * setBuildingType(BuildingType.CHAIN, "starbucks")
     *
     * then you'll only get Starbucks nearby.
     */
    void setBuildingType(BuildingType typeKey, String typeVal);

    // Get a list of buildings which should be visible in the current camera feed.
    List<Building> getBuildingsInView();

    // Get the building, if it exists, which is "in focus" i.e. in center of camera feed
    // and closest to the phone.
    // "If it exists" is handled by Optional class
    // https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html
    Optional<Building> getBuildingInFocus();

    // For testing
    BuildingSpatialIndex.LocalPose localPose();
}
