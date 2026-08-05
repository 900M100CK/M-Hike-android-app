package com.example.m_hikeapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.m_hikeapp.databinding.ActivityHikeMapBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.repository.HikeRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

/**
 * Activity displaying the location of a hike on Mapbox map.
 */
public class HikeMapActivity extends AppCompatActivity {

    public static final String EXTRA_HIKE_ID = "extra_hike_id";
    private static final int REQUEST_LOCATION_PERMISSION = 200;

    private ActivityHikeMapBinding binding;
    private HikeRepository repository;
    private FusedLocationProviderClient fusedLocationClient;
    private long hikeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHikeMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = HikeRepository.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        hikeId = getIntent().getLongExtra(EXTRA_HIKE_ID, -1L);

        setupToolbar();
        loadHikeAndInitializeMap();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadHikeAndInitializeMap() {
        if (hikeId == -1L) {
            Toast.makeText(this, R.string.error_invalid_hike, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository.getHikeById(hikeId, hike -> {
            if (hike == null) {
                Toast.makeText(this, R.string.error_hike_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(hike.getName());
            }

            binding.mapView.getMapboxMap().loadStyleUri(Style.STANDARD, style -> {
                // Check if lat/long coordinates exist, default to Snowdonia/UK or center if available
                double lat = hike.getLatitude() != null ? hike.getLatitude() : 53.0685;
                double lon = hike.getLongitude() != null ? hike.getLongitude() : -4.0763;

                Point point = Point.fromLngLat(lon, lat);
                centerMapOnPoint(point, 13.0);

                // Add Point Marker Annotation using Mapbox Annotation Plugin
                com.mapbox.maps.plugin.annotation.AnnotationPlugin annotationPlugin =
                        com.mapbox.maps.plugin.annotation.AnnotationsUtils.getAnnotations(binding.mapView);
                com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager pointAnnotationManager =
                        com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new com.mapbox.maps.plugin.annotation.AnnotationConfig());

                // Convert ic_map_marker drawable to Bitmap for Mapbox annotation
                android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_map_marker);
                android.graphics.Bitmap bitmap = null;
                if (drawable != null) {
                    bitmap = android.graphics.Bitmap.createBitmap(
                            drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() * 2 : 96,
                            drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() * 2 : 96,
                            android.graphics.Bitmap.Config.ARGB_8888
                    );
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    androidx.core.graphics.drawable.DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.md_primary));
                    drawable.draw(canvas);
                }

                com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions pointAnnotationOptions =
                        new com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions()
                                .withPoint(point);

                if (bitmap != null) {
                    pointAnnotationOptions.withIconImage(bitmap);
                } else {
                    pointAnnotationOptions.withTextField(hike.getName());
                }

                pointAnnotationManager.create(pointAnnotationOptions);

                // Setup FAB click handlers: Recenter to Phone GPS Location
                binding.fabRecenter.setOnClickListener(v -> moveToDeviceLocation(point));

                binding.fabZoomIn.setOnClickListener(v -> {
                    double currentZoom = binding.mapView.getMapboxMap().getCameraState().getZoom();
                    binding.mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(currentZoom + 1.0).build());
                });

                binding.fabZoomOut.setOnClickListener(v -> {
                    double currentZoom = binding.mapView.getMapboxMap().getCameraState().getZoom();
                    binding.mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(currentZoom - 1.0).build());
                });
            });
        });
    }

    private void moveToDeviceLocation(Point fallbackPoint) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Point myLocationPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                        centerMapOnPoint(myLocationPoint, 15.0);
                        Toast.makeText(HikeMapActivity.this, "Centered to your current position", Toast.LENGTH_SHORT).show();
                    } else {
                        // Fallback to last known position or hike position
                        centerMapOnPoint(fallbackPoint, 14.0);
                        Toast.makeText(HikeMapActivity.this, "Could not fetch current GPS, showing hike position", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    centerMapOnPoint(fallbackPoint, 14.0);
                    Toast.makeText(HikeMapActivity.this, "GPS error, showing hike position", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            binding.fabRecenter.performClick();
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void centerMapOnPoint(Point point, double zoom) {
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(point)
                .zoom(zoom)
                .build();
        binding.mapView.getMapboxMap().setCamera(cameraOptions);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
