package com.example.m_hikeapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hikeapp.databinding.ActivityHikeMapBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.repository.HikeRepository;
import com.example.m_hikeapp.util.GpsUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Trailhead location map screen (Feature G1).
 * <p>
 * Requires the {@link HikeListActivity#EXTRA_HIKE_ID} extra (the id of the hike
 * whose trailhead should be shown). When the hike has valid GPS coordinates a
 * single branded marker is dropped at the trailhead and the camera is zoomed to
 * level 14; otherwise a "No location saved" empty state is displayed.
 */
public class HikeMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    /** Camera zoom level applied at the trailhead. */
    private static final float TRAILHEAD_ZOOM = 14f;

    private ActivityHikeMapBinding binding;
    private HikeRepository         repository;
    private long                   hikeId;

    private GoogleMap googleMap;
    private Hike      loadedHike;

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

        setupToolbar();
        setupMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHike();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadHike() {
        repository.getHikeById(hikeId, hike -> {
            if (hike == null) {
                Toast.makeText(this, R.string.error_hike_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            loadedHike = hike;
            renderMap();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        renderMap();
    }

    private void renderMap() {
        if (googleMap == null || loadedHike == null) {
            return;
        }
        if (GpsUtils.isValid(loadedHike.getLatitude(), loadedHike.getLongitude())) {
            binding.mapContainer.setVisibility(View.VISIBLE);
            binding.textNoLocation.setVisibility(View.GONE);

            LatLng trailhead = new LatLng(loadedHike.getLatitude(), loadedHike.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions()
                    .position(trailhead)
                    .title(loadedHike.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(trailhead, TRAILHEAD_ZOOM));
        } else {
            binding.mapContainer.setVisibility(View.GONE);
            binding.textNoLocation.setVisibility(View.VISIBLE);
        }
    }
}
