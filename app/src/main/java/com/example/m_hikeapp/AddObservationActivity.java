package com.example.m_hikeapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hikeapp.databinding.ActivityAddObservationBinding;
import com.example.m_hikeapp.model.Observation;
import com.example.m_hikeapp.repository.HikeRepository;
import com.example.m_hikeapp.util.ValidationResult;
import com.example.m_hikeapp.util.ValidationUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * Form Activity for adding or editing an {@link Observation} (Feature C).
 *
 * <h3>Extras expected</h3>
 * <ul>
 *   <li>{@link HikeListActivity#EXTRA_HIKE_ID} – {@code long} – always required.</li>
 *   <li>{@link HikeDetailActivity#EXTRA_OBSERVATION_ID} – {@code long} – present only in edit mode.</li>
 * </ul>
 */
public class AddObservationActivity extends AppCompatActivity {

    // -------------------------------------------------------------------------
    private ActivityAddObservationBinding binding;
    private HikeRepository               repository;

    private long        hikeId;
    private String      selectedTime = "";
    /** Non-null in edit mode. */
    private Observation existingObservation = null;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding    = ActivityAddObservationBinding.inflate(getLayoutInflater());
        repository = HikeRepository.getInstance(this);
        setContentView(binding.getRoot());

        hikeId = getIntent().getLongExtra(HikeListActivity.EXTRA_HIKE_ID, -1L);
        if (hikeId == -1L) {
            Toast.makeText(this, R.string.error_invalid_hike, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupTimePicker();
        setupSaveButton();

        long observationId = getIntent().getLongExtra(HikeDetailActivity.EXTRA_OBSERVATION_ID, -1L);
        if (observationId != -1L) {
            loadObservationForEditing(observationId);
        }
    }

    // -------------------------------------------------------------------------
    // UI setup
    // -------------------------------------------------------------------------

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            boolean isEdit = getIntent().hasExtra(HikeDetailActivity.EXTRA_OBSERVATION_ID);
            getSupportActionBar().setTitle(isEdit ? R.string.title_edit_observation : R.string.title_add_observation);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTimePicker() {
        // Default to current time
        Calendar now = Calendar.getInstance();
        int hour   = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        selectedTime = String.format(Locale.US, "%02d:%02d", hour, minute);
        binding.editTextObsTime.setText(selectedTime);
        binding.editTextObsTime.setFocusable(false);
        binding.editTextObsTime.setOnClickListener(v -> showTimePicker());
        binding.inputLayoutObsTime.setEndIconOnClickListener(v -> showTimePicker());
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                    binding.editTextObsTime.setText(selectedTime);
                    binding.inputLayoutObsTime.setError(null);
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true // 24-hour format
        ).show();
    }

    private void setupSaveButton() {
        binding.buttonSaveObservation.setOnClickListener(v -> {
            Observation obs = collectFormValues();
            ValidationResult result = ValidationUtils.validateObservation(obs);
            if (result.isValid()) {
                persistObservation(obs);
            } else {
                applyValidationErrors(result);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Edit mode
    // -------------------------------------------------------------------------

    private void loadObservationForEditing(long observationId) {
        // ObservationDao.getById is not exposed on the repository; we work around by
        // loading all observations for this hike and finding the one we need.
        // A production app would expose a direct getById on the repository.
        binding.buttonSaveObservation.setEnabled(false);
        // Direct DAO access via inner executor (kept simple for coursework scope):
        new Thread(() -> {
            com.example.m_hikeapp.database.DatabaseHelper db =
                    com.example.m_hikeapp.database.DatabaseHelper.getInstance(this);
            com.example.m_hikeapp.dao.ObservationDao dao =
                    new com.example.m_hikeapp.dao.ObservationDao(db);
            Observation obs = dao.getById(observationId);
            runOnUiThread(() -> {
                binding.buttonSaveObservation.setEnabled(true);
                if (obs == null) {
                    Toast.makeText(this, R.string.error_observation_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                existingObservation = obs;
                populateForm(obs);
            });
        }).start();
    }

    private void populateForm(Observation obs) {
        binding.editTextObsTitle.setText(obs.getTitle());
        binding.editTextObsTime.setText(obs.getObsTime());
        selectedTime = obs.getObsTime();
        binding.editTextObsComment.setText(obs.getComment());
    }

    // -------------------------------------------------------------------------
    // Form helpers
    // -------------------------------------------------------------------------

    private Observation collectFormValues() {
        Observation obs = (existingObservation != null) ? existingObservation : new Observation();
        obs.setHikeId(hikeId);
        obs.setTitle(getText(binding.editTextObsTitle));
        obs.setObsTime(selectedTime);
        obs.setComment(getText(binding.editTextObsComment));
        return obs;
    }

    private String getText(android.widget.EditText editText) {
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }

    private void applyValidationErrors(ValidationResult result) {
        binding.inputLayoutObsTitle.setError(result.getError(ValidationUtils.FIELD_OBS_TITLE));
        binding.inputLayoutObsTime.setError(result.getError(ValidationUtils.FIELD_OBS_TIME));
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    private void persistObservation(Observation obs) {
        binding.buttonSaveObservation.setEnabled(false);
        HikeRepository.OperationCallback callback = (success, message) -> {
            binding.buttonSaveObservation.setEnabled(true);
            if (success) {
                Toast.makeText(this, R.string.msg_observation_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        };

        if (existingObservation != null) {
            repository.updateObservation(obs, callback);
        } else {
            repository.addObservation(obs, callback);
        }
    }
}
