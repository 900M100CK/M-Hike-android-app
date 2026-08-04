package com.example.m_hikeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.m_hikeapp.adapter.ObservationAdapter;
import com.example.m_hikeapp.databinding.ActivityHikeDetailBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;
import com.example.m_hikeapp.repository.HikeRepository;

/**
 * Detail screen for a single hike (Feature B + C).
 *
 * <p>Displays full hike details and lists all associated observations.
 * Provides navigation to {@link AddHikeActivity} for editing the hike, and to
 * {@link AddObservationActivity} for adding/editing observations.</p>
 *
 * <h3>Extras expected</h3>
 * <ul>
 *   <li>{@link HikeListActivity#EXTRA_HIKE_ID} – {@code long} – required.</li>
 * </ul>
 */
public class HikeDetailActivity extends AppCompatActivity
        implements ObservationAdapter.ObservationClickListener {

    public static final String EXTRA_OBSERVATION_ID = "extra_observation_id";

    // -------------------------------------------------------------------------
    // ViewBinding & dependencies
    // -------------------------------------------------------------------------
    private ActivityHikeDetailBinding binding;
    private HikeRepository            repository;
    private ObservationAdapter        obsAdapter;
    private long                      hikeId;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding    = ActivityHikeDetailBinding.inflate(getLayoutInflater());
        repository = HikeRepository.getInstance(this);
        setContentView(binding.getRoot());

        hikeId = getIntent().getLongExtra(HikeListActivity.EXTRA_HIKE_ID, -1L);
        if (hikeId == -1L) {
            Toast.makeText(this, R.string.error_invalid_hike, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupObservationsList();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHikeDetails();
        loadObservations();
    }

    // -------------------------------------------------------------------------
    // Setup helpers
    // -------------------------------------------------------------------------

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupObservationsList() {
        obsAdapter = new ObservationAdapter(this);
        binding.recyclerObservations.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerObservations.setAdapter(obsAdapter);
        // Disable nested scrolling so the outer ScrollView handles all scrolling.
        binding.recyclerObservations.setNestedScrollingEnabled(false);
    }

    private void setupButtons() {
        // View Hike Map
        binding.buttonViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, HikeMapActivity.class);
            intent.putExtra(HikeMapActivity.EXTRA_HIKE_ID, hikeId);
            startActivity(intent);
        });

        // Edit this hike
        binding.buttonEditHike.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddHikeActivity.class);
            intent.putExtra(HikeListActivity.EXTRA_HIKE_ID, hikeId);
            startActivity(intent);
        });

        // Add a new observation
        binding.buttonAddObservation.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddObservationActivity.class);
            intent.putExtra(HikeListActivity.EXTRA_HIKE_ID, hikeId);
            startActivity(intent);
        });
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    private void loadHikeDetails() {
        repository.getHikeById(hikeId, hike -> {
            if (hike == null) {
                Toast.makeText(this, R.string.error_hike_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            populateDetails(hike);
        });
    }

    private void loadObservations() {
        repository.getObservationsForHike(hikeId, observations -> {
            obsAdapter.submitList(observations);
            binding.textNoObservations.setVisibility(
                    observations.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    // -------------------------------------------------------------------------
    // Detail population
    // -------------------------------------------------------------------------

    /** Populates all detail text views from a loaded {@link Hike}. */
    private void populateDetails(Hike hike) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(hike.getName());
        }
        binding.textDetailName.setText(hike.getName());
        binding.textDetailLocation.setText(hike.getLocation());
        binding.textDetailDate.setText(hike.getDate());
        binding.textDetailParking.setText(hike.isParkingAvailable()
                ? getString(R.string.label_parking_yes)
                : getString(R.string.label_parking_no));
        binding.textDetailLength.setText(
                String.format(java.util.Locale.getDefault(), "%.1f km", hike.getLengthKm()));
        binding.textDetailDifficulty.setText(hike.getDifficulty());

        // Optional fields
        setOptionalField(binding.textDetailDescription, hike.getDescription());
        setOptionalField(binding.textDetailCustom1, hike.getCustomField1());
        setOptionalField(binding.textDetailCustom2, hike.getCustomField2());
    }

    /** Shows the optional field's value or hides its row when empty. */
    private void setOptionalField(android.widget.TextView textView, String value) {
        if (value != null && !value.isEmpty()) {
            textView.setText(value);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    // -------------------------------------------------------------------------
    // ObservationClickListener implementation
    // -------------------------------------------------------------------------

    @Override
    public void onEditObservation(Observation observation) {
        Intent intent = new Intent(this, AddObservationActivity.class);
        intent.putExtra(HikeListActivity.EXTRA_HIKE_ID, hikeId);
        intent.putExtra(EXTRA_OBSERVATION_ID, observation.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteObservation(Observation observation) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_obs_title)
                .setMessage(getString(R.string.dialog_delete_obs_message, observation.getTitle()))
                .setPositiveButton(R.string.action_delete, (d, w) ->
                        repository.deleteObservation(observation.getId(), (success, msg) -> {
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            loadObservations();
                        }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
