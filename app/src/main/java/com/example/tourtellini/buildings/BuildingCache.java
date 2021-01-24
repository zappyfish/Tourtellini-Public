package com.example.tourtellini.buildings;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.android.volley.VolleyError;
import com.example.tourtellini.apis.TourtelliniApiCallback;
import com.example.tourtellini.apis.TourtelliniApiRequest;
import com.example.tourtellini.phone.pose.PhonePose;
import com.example.tourtellini.phone.pose.PoseUpdateCallback;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Point;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import rx.Observable;

public class BuildingCache implements PoseUpdateCallback {

    // TODO: Change these? maybe?
    private final static int MIN_CACHE_RADIUS_M = 40;
    private final static int REBUILD_RADIUS_M = 100;

    // TODO: REDUCE VALUE OF THIS AFTER TESTING
    // `MAX_GAZE_M` is the max distance a building can be from the last pose in order for it
    // to be considered "in view".
    private final static float MAX_GAZE_M = 50;
    // This is the horizontal field of view that we want to search in (on both sides). Might want to play
    // around with it.
    private final static float HORIZONTAL_GAZE_ANGLE_DEGREES = 12;

    private final static String BUILDING_REQUEST_ENDPOINT = "/buildings/nearby";
    private final static String LAT_KEY = "lat";
    private final static String LNG_KEY = "lng";

    private BuildingSpatialIndex mBuildingSpatialIndex;

    private final Context mContext;
    private final Map<String, String> mBuildingRequestParams;

    private boolean mCurrentlyRebuilding;
    private PhonePose mLastPose;

    BuildingCache(Context context, String searchCategoryKey, String searchCategoryVal) {
        mContext = context;
        mBuildingRequestParams = new HashMap<>();
        mCurrentlyRebuilding = false;

        mBuildingRequestParams.put("radius", String.valueOf(REBUILD_RADIUS_M));

        // e.g. key = "categories", val = "food-beverage"
        mBuildingRequestParams.put(searchCategoryKey, searchCategoryVal);
    }

    private synchronized void rebuildCache(final PhonePose newPose) {
        if (!shouldRebuild(newPose)) {
            return;
        }

        if (newPose.lng() == 0 || newPose.lat() == 0) {
            // don't have a valid pose yet
            return;
        }

        Log.e("Rebuliding", "Rebuilding");

        // Set flag so we don't make repeat requests.
        mCurrentlyRebuilding = true;

        // Set new lat, lng params.
        mBuildingRequestParams.put(LAT_KEY, String.valueOf(newPose.lat()));
        mBuildingRequestParams.put(LNG_KEY, String.valueOf(newPose.lng()));

        TourtelliniApiRequest.getInstance(mContext).makeRequest(BUILDING_REQUEST_ENDPOINT,
                mBuildingRequestParams,
                new TourtelliniApiCallback() {
                    @Override
                    public void onResponse(JSONArray responseData) {
                        BuildingSpatialIndex.Builder builder = new BuildingSpatialIndex.Builder(newPose);

                        for (int i = 0; i < responseData.length(); i++) {
                            try {
                                Building building = Building.fromJson(responseData.getJSONObject(i));
                                builder.addBuilding(building);
                            } catch (Exception e) {
                                // TODO: something
                            }
                        }

                        // TODO: do this more safely, we might get a race condition.
                        mBuildingSpatialIndex = builder.build();
                        Log.e("Building", "Done Building");
                        mCurrentlyRebuilding = false;
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Building", "Response yielded error");
                        Log.e("Volley error", error.toString());
                        mCurrentlyRebuilding = false;
                    }
                }
        );

    }

    private float minRadiusAvailable(PhonePose newPose) {
        // Subtract distance from the origin from the overall rebuild radius.
        return REBUILD_RADIUS_M - mBuildingSpatialIndex.projectToLocal(newPose).dist();
    }

    // We rebuild if we've gone too far from the origin and don't have at least
    // `MIN_CACHE_RADIUS_M` radius cached.
    private boolean shouldRebuild(PhonePose newPose) {
         return !mCurrentlyRebuilding && (mBuildingSpatialIndex == null || (minRadiusAvailable(newPose) < MIN_CACHE_RADIUS_M));
    }

    @Override
    public void onPoseUpdate(PhonePose newPose) {
        if (shouldRebuild(newPose)) {
            rebuildCache(newPose);
        }

        mLastPose = newPose;
    }

    private static boolean validPitch(BuildingSpatialIndex.LocalPose lastPoseLocal) {
        // quick hack, make betterer
        return lastPoseLocal.pitchDegrees() >= -25 && lastPoseLocal.pitchDegrees() <= 20;
    }

    private PointF getNewPoint(PointF unitVecOrientation, float rotRads, float distanceMagnitude) {
        final float x = unitVecOrientation.x;
        final float y = unitVecOrientation.y;

        final float x_rot = (float) (x * Math.cos(rotRads) - y * Math.sin(rotRads));
        final float y_rot = (float) (x * Math.sin(rotRads) + y * Math.cos(rotRads));

        return new PointF(x_rot * distanceMagnitude, y_rot * distanceMagnitude);
    }

    private float sign(PointF p1, PointF p2, PointF p3) {
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }

    boolean pointInTriangle(PointF pt, PointF v1, PointF v2, PointF v3) {
        float d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(pt, v1, v2);
        d2 = sign(pt, v2, v3);
        d3 = sign(pt, v3, v1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    private List<Entry<Building, Point>> computeBuildingsInView() {
        List<Entry<Building, Point>> buildings = new LinkedList<>();

        BuildingSpatialIndex.LocalPose lastPoseLocal = lastPoseLocal();
        if (!validPitch(lastPoseLocal)) {
            return buildings;
        }

        PointF uvec = lastPoseLocal.orientationUnitVector();

        PointF p0 = new PointF(lastPoseLocal.t_x(), lastPoseLocal.t_y());

        float minX = p0.x - MAX_GAZE_M;
        float minY = p0.y - MAX_GAZE_M;

        float maxX = p0.x + MAX_GAZE_M;
        float maxY = p0.y + MAX_GAZE_M;

        PointF searchBoxMin = new PointF(minX, minY);
        PointF searchBoxMax = new PointF(maxX, maxY);

        // First narrow down to buildings in our search box.
        Observable<Entry<Building, Point>> buildingsNearbyCoarse =
                mBuildingSpatialIndex.buildingsWithin(searchBoxMin, searchBoxMax);

        PointF p1 = getNewPoint(uvec, -HORIZONTAL_GAZE_ANGLE_DEGREES, MAX_GAZE_M);
        PointF p2 = getNewPoint(uvec, HORIZONTAL_GAZE_ANGLE_DEGREES, MAX_GAZE_M);

        // Then select only buildings within our search triangle.
        for (Entry<Building, Point> entry : buildingsNearbyCoarse.toBlocking().toIterable()) {
            PointF buildingLocalPoint = new PointF((float)entry.geometry().x(), (float)entry.geometry().y());

            if (pointInTriangle(buildingLocalPoint, p0, p1, p2)) {
                buildings.add(entry);
            }
        }

        return buildings;
    }

    List<Building> buildingsInView() {
        List<Entry<Building, Point>> buildings = computeBuildingsInView();
        List<Building> ret = new LinkedList<>();

        for (Entry<Building, Point> entry : buildings) {
            ret.add(entry.value());
        }

        return ret;
    }

    float angleBetween(PointF p0, PointF a, PointF b) {
        PointF v1 = new PointF(a.x - p0.x, a.y - p0.y);
        PointF v2 = new PointF(b.x - p0.x, b.y - p0.y);

        float dot = (v1.x * v2.x) + (v1.y * v2.y);
        return (float) Math.acos((dot) / (v1.length() * v2.length()));
    }

    Optional<Building> buildingInFocus() {
        List<Entry<Building, Point>> buildingsInView = computeBuildingsInView();
        if (buildingsInView.isEmpty()) {
            return Optional.empty();
        }

        Building closest = null;
        PointF uvec = lastPoseLocal().orientationUnitVector();

        PointF p0 = new PointF(lastPoseLocal().t_x(), lastPoseLocal().t_y());
        PointF a = getNewPoint(uvec, 0, MAX_GAZE_M);

        float minAngle = (float) Math.PI;
        for (Entry<Building, Point> entry : buildingsInView) {
            PointF b = new PointF((float)entry.geometry().x(), (float)entry.geometry().y());
            float angle = angleBetween(p0, a, b);
            if (angle < minAngle || closest == null) {
                closest = entry.value();
                minAngle = angle;
            }

            if (entry.value().getName().equals("Starbucks")) {
                // Log.e("p0", p0.toString());
                // Log.e("a", a.toString());
                // Log.e("b", b.toString());
            }
        }

        return Optional.of(closest);
    }

    BuildingSpatialIndex.LocalPose lastPoseLocal() {
        if (mLastPose == null || mBuildingSpatialIndex == null) {
            return null;
        }

        return mBuildingSpatialIndex.projectToLocal(mLastPose);
    }
}
