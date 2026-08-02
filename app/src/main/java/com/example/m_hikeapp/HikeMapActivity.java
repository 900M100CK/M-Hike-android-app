package com.example.m_hikeapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.m_hikeapp.databinding.ActivityHikeMapBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.repository.HikeRepository;
import com.example.m_hikeapp.util.GpsUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Trailhead location map screen (Feature G1).
 * <p>
 * Requires the {@link HikeListActivity#EXTRA_HIKE_ID} extra (the id of the hike
 * whose trailhead should be shown). When the hike has valid GPS coordinates a
 * single branded marker is dropped at the trailhead and the camera is zoomed to
 * level 14; otherwise a "No location saved" empty state is displayed.
 * <p>
 * Rendered with osmdroid (OpenStreetMap) — no Google API key required.
 */
public class HikeMapActivity extends AppCompatActivity {

    /** Camera zoom level applied at the trailhead. */
    private static final float TRAILHEAD_ZOOM = 14f;

    /** Camera zoom level applied while following the live location. */
    private static final float LIVE_ZOOM = 17f;

    /** Location request cadence while tracking. */
    private static final long UPDATE_INTERVAL_MS = 2000L;

    /** Minimum accepted interval between location updates. */
    private static final long FASTEST_INTERVAL_MS = 1000L;

    /** Width (in px) of the traveled-path polyline. */
    private static final float PATH_WIDTH_PX = 8f;

    private static final int REQUEST_LOCATION_PERMISSION = 42;

    private ActivityHikeMapBinding binding;
    private HikeRepository         repository;
    private long                   hikeId;

    private MapView mapView;
    private Hike    loadedHike;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest             locationRequest;
    private LocationCallback            locationCallback;
    private Marker                      liveLocationMarker;
    private Polyline                    pathPolyline;
    private final List<GeoPoint>        pathPoints = new ArrayList<>();
    private boolean                     isTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding    = ActivityHikeMapBinding.inflate(getLayoutInflater());
        repository = HikeRepository.getInstance(this);
        setContentView(binding.getRoot());

        hikeId = getIntent().getLongExtra(HikeListActivity.EXTRA_HIKE_ID, -1L);
        if (hikeId == -1L) {
            Toast.makeText(this, R.string.error_invalid_hike, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupToolbar();
        setupMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        loadHike();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationTracking();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationTracking();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupMap() {
        Configuration.getInstance().load(
                this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        mapView = binding.map;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(TRAILHEAD_ZOOM);
    }

    private void loadHike() {
        repository.getHikeById(hikeId, hike -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (hike == null) {
                Toast.makeText(this, R.string.error_hike_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            loadedHike = hike;
            renderMap();
        });
    }

    private void renderMap() {
        if (mapView == null || loadedHike == null) {
            return;
        }
        if (GpsUtils.isValid(loadedHike.getLatitude(), loadedHike.getLongitude())) {
            binding.mapContainer.setVisibility(View.VISIBLE);
            binding.textNoLocation.setVisibility(View.GONE);

            GeoPoint trailhead =
                    new GeoPoint(loadedHike.getLatitude(), loadedHike.getLongitude());
            mapView.getOverlays().clear();
            Marker trailheadMarker = new Marker(mapView);
            trailheadMarker.setPosition(trailhead);
            trailheadMarker.setTitle(loadedHike.getName());
            trailheadMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map_marker));
            trailheadMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(trailheadMarker);

            // Re-add path and live marker if they exist (e.g. after rotation or resume)
            if (pathPolyline != null && !mapView.getOverlays().contains(pathPolyline)) {
                mapView.getOverlays().add(pathPolyline);
            }
            if (liveLocationMarker != null && !mapView.getOverlays().contains(liveLocationMarker)) {
                mapView.getOverlays().add(liveLocationMarker);
            }

            mapView.getController().setZoom(TRAILHEAD_ZOOM);
            mapView.getController().animateTo(trailhead);

            startLocationTracking(trailhead);
        } else {
            binding.mapContainer.setVisibility(View.VISIBLE);
            binding.textNoLocation.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Kicks off realtime road tracking. The trailhead is used as the first
     * point of the traveled path; live fixes refresh the blue polyline and a
     * dedicated marker while the camera follows the device.
     */
    private void startLocationTracking(GeoPoint trailhead) {
        if (isTracking) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.permission_location_rationale)
                        .setPositiveButton(R.string.action_continue, (dialog, which) ->
                                ActivityCompat.requestPermissions(
                                        this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION))
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
            return;
        }

        isTracking = true;
        // Don't clear if returning from a pause; only clear if starting fresh
        if (pathPoints.isEmpty()) {
            pathPoints.add(trailhead);
        }

        if (pathPolyline == null) {
            pathPolyline = new Polyline(mapView);
            pathPolyline.setColor(Color.BLUE);
            pathPolyline.setWidth(PATH_WIDTH_PX);
        }
        pathPolyline.setPoints(pathPoints);

        if (!mapView.getOverlays().contains(pathPolyline)) {
            mapView.getOverlays().add(pathPolyline);
        }
        mapView.invalidate();

        buildLocationRequest();
        buildLocationCallback();
        fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                .build();
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location == null) {
                    return;
                }
                GeoPoint current =
                        new GeoPoint(location.getLatitude(), location.getLongitude());
                pathPoints.add(current);
                if (pathPolyline != null) {
                    pathPolyline.setPoints(pathPoints);
                }
                if (liveLocationMarker == null) {
                    liveLocationMarker = new Marker(mapView);
                    liveLocationMarker.setPosition(current);
                    liveLocationMarker.setIcon(ContextCompat.getDrawable(
                            HikeMapActivity.this, R.drawable.ic_location));
                    liveLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    mapView.getOverlays().add(liveLocationMarker);
                } else {
                    liveLocationMarker.setPosition(current);
                }
                mapView.getController().animateTo(current);
                // Only auto-zoom on the first few fixes so the user can zoom out to see the trail
                if (pathPoints.size() <= 2) {
                    mapView.getController().setZoom(LIVE_ZOOM);
                }
                mapView.invalidate();
            }
        };
    }

    private void stopLocationTracking() {
        isTracking = false;
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

@Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_LOCATION_PERMISSION) {
            return;
        }
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            GeoPoint trailhead = loadedHike != null
                    ? new GeoPoint(loadedHike.getLatitude(), loadedHike.getLongitude())
                    : null;
            if (trailhead != null && GpsUtils.isValid(
                    loadedHike.getLatitude(), loadedHike.getLongitude())) {
                startLocationTracking(trailhead);
            }
        } else {
            Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_LONG).show();
        }
    }
}
