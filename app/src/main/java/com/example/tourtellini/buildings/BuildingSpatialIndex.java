package com.example.tourtellini.buildings;

import android.graphics.PointF;
import android.util.Log;

import com.example.tourtellini.phone.pose.PhonePose;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;

public class BuildingSpatialIndex {

    private static final float METERS_PER_DEGREE_FACTOR = 111111f;

    private final PointF mLatLngOrigin;
    private final RTree<Building, Point> mRTree;


    private BuildingSpatialIndex(PointF latLngOrigin, RTree<Building, Point> rTree) {
        mLatLngOrigin = latLngOrigin;
        mRTree = rTree;
    }

    PointF getLatLngOrigin() {
        return mLatLngOrigin;
    }


    // Because our local building cache is small (a few hundred meters at a time), we use the
    // quick and dirty assumption that 111,111m in the y direction is 1 degree of latitude and
    // 111,111 * cos(latitude)m in the x direction is 1 degree of longitude.
    // Note that this assumption breaks down badly at the North and South poles. Sorry, penguins!
    static PointF projectToLocal(double lat, double lng, double latOrigin, double lngOrigin) {
        final float t_y_meters = (float) ((lat - latOrigin) * METERS_PER_DEGREE_FACTOR);
        final float t_x_meters = (float) ((lng - lngOrigin) * METERS_PER_DEGREE_FACTOR / Math.cos(lngOrigin));

        return new PointF(t_x_meters, t_y_meters);
    }


    LocalPose projectToLocal(PhonePose phonePose) {
        PointF localPosition = projectToLocal(phonePose.lat(), phonePose.lng(), mLatLngOrigin.x, mLatLngOrigin.y);
        return new LocalPose(localPosition.x, localPosition.y, phonePose.roll(), phonePose.pitch(), phonePose.yaw());
    }

    // buildings that are within the rectangle with corners at min (lower left) and max (upper right)
    Observable<Entry<Building, Point>> buildingsWithin(PointF min, PointF max) {
        LinkedList<Building> buildings = new LinkedList<>();

        return mRTree.search(Geometries.rectangle(min.x, min.y, max.x, max.y));
    }

    static class Builder {

        private final PhonePose mPhonePoseOrigin;
        private RTree<Building, Point> mRTree;

        Builder(PhonePose phonePoseOrigin) {
            mPhonePoseOrigin = phonePoseOrigin;
            mRTree = RTree.create();
        }

        Builder addBuilding(Building building) {
            PointF localPosition = projectToLocal(building.lat(), building.lng(), mPhonePoseOrigin.lat(), mPhonePoseOrigin.lng());
            mRTree = mRTree.add(building, Geometries.point(localPosition.x, localPosition.y));
            return this;
        }

        BuildingSpatialIndex build() {
            return new BuildingSpatialIndex(new PointF(mPhonePoseOrigin.lat(), mPhonePoseOrigin.lng()), mRTree);
        }
    }

    public static class LocalPose {

        private final PointF mCoords;
        private final PointF mOrientation;
        private final float mPitch;

        // No translation z component because in our little world, the Earth is flat.
        private LocalPose(float t_x, float t_y, float roll, float pitch, float yaw) {
            mCoords = new PointF(t_x, t_y);
            mPitch = pitch;

            final float r_x = (float) (Math.cos(yaw) * Math.cos(pitch));
            final float r_y = (float) (Math.sin(yaw) * Math.cos(pitch));

            // flip
            mOrientation = new PointF(r_y, r_x);

            // Log.w("Orientation", mOrientation.toString());
        }

        @Override
        public String toString() {
            return String.format("p: (%f, %f) ornt: (%1.2f, %1.2f) pitch: %f", mCoords.x, mCoords.y,
                    mOrientation.x, mOrientation.y, mPitch);
        }

        float dist() {
            return mCoords.length();
        }

        public float t_x() {
            return mCoords.x;
        }

        public float t_y() {
            return mCoords.y;
        }

        public float pitchDegrees() {
            return (float) (mPitch * 180 / Math.PI);
        }

        public PointF orientationUnitVector() {
            return mOrientation;
        }
    }
}
