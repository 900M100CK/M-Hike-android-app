package com.example.m_hikeapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hikeapp.databinding.ActivityAddHikeBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.repository.HikeRepository;
import com.example.m_hikeapp.util.ValidationResult;
import com.example.m_hikeapp.util.ValidationUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * Form Activity for creating a new hike or editing an existing one (Feature A).
 *
 * <h3>Flow</h3>
 * <ol>
 *   <li>User fills in all fields.</li>
 *   <li>On "Preview" tap → {@link ValidationUtils} validates all fields and
 *       highlights inline errors via {@code TextInputLayout.setError()}.</li>
 *   <li>If valid → confirmation dialog summarises all values.</li>
 *   <li>On "Save" in dialog → {@link HikeRepository#addHike} or
 *       {@link HikeRepository#updateHike} is called asynchronously.</li>
 * </ol>
 *
 * <h3>Edit mode</h3>
 * <p>Pass {@link HikeListActivity#EXTRA_HIKE_ID} in the launching Intent to
 * pre-populate all fields for editing an existing hike.</p>
 */
public class AddHikeActivity extends AppCompatActivity {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    // -------------------------------------------------------------------------
    // ViewBinding & dependencies
    // -------------------------------------------------------------------------
    private ActivityAddHikeBinding binding;
    private HikeRepository         repository;

    /** Non-null when editing an existing hike; null when adding a new one. */
    private Hike existingHike = null;

    /** Selected date stored as "yyyy-MM-dd". */
    private String selectedDate = "";

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding    = ActivityAddHikeBinding.inflate(getLayoutInflater());
        repository = HikeRepository.getInstance(this);
        setContentView(binding.getRoot());

        setupToolbar();
        setupDifficultySpinner();
        setupDatePicker();
        setupSaveButton();

        // Check if we are in edit mode.
        long hikeId = getIntent().getLongExtra(HikeListActivity.EXTRA_HIKE_ID, -1L);
        if (hikeId != -1L) {
            loadHikeForEditing(hikeId);
        }
    }

    // -------------------------------------------------------------------------
    // UI Setup
    // -------------------------------------------------------------------------

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            boolean isEdit = getIntent().hasExtra(HikeListActivity.EXTRA_HIKE_ID);
            getSupportActionBar().setTitle(isEdit ? R.string.title_edit_hike : R.string.title_add_hike);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Populates the difficulty {@link android.widget.Spinner} with the
     * four accepted levels from {@link Hike}.
     */
    private void setupDifficultySpinner() {
        String[] levels = {
                Hike.DIFFICULTY_EASY,
                Hike.DIFFICULTY_MODERATE,
                Hike.DIFFICULTY_HARD,
                Hike.DIFFICULTY_EXPERT
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, levels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDifficulty.setAdapter(spinnerAdapter);
    }

    /**
     * Wires the date field so a {@link DatePickerDialog} opens on tap.
     * Sets today's date as the default.
     */
    private void setupDatePicker() {
        // Set today as default
        Calendar today = Calendar.getInstance();
        selectedDate = String.format(Locale.US, "%04d-%02d-%02d",
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH));
        binding.editTextDate.setText(selectedDate);
        binding.editTextDate.setFocusable(false);
        binding.editTextDate.setOnClickListener(v -> showDatePicker());
        binding.inputLayoutDate.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                    binding.editTextDate.setText(selectedDate);
                    binding.inputLayoutDate.setError(null); // Clear any previous error
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /** Wires the "Preview & Save" button to trigger validation then a summary dialog. */
    private void setupSaveButton() {
        binding.buttonPreviewSave.setOnClickListener(v -> {
            Hike hike = collectFormValues();
            ValidationResult result = ValidationUtils.validateHike(hike);
            if (result.isValid()) {
                showSummaryDialog(hike);
            } else {
                applyValidationErrors(result);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Edit mode – pre-populate form
    // -------------------------------------------------------------------------

    /**
     * Loads the existing hike from the repository and populates the form fields.
     *
     * @param hikeId Primary key of the hike to load.
     */
    private void loadHikeForEditing(long hikeId) {
        binding.buttonPreviewSave.setEnabled(false); // Disable while loading
        repository.getHikeById(hikeId, hike -> {
            binding.buttonPreviewSave.setEnabled(true);
            if (hike == null) {
                Toast.makeText(this, R.string.error_hike_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            existingHike = hike;
            populateForm(hike);
        });
    }

    /** Fills all form fields from an existing {@link Hike}. */
    private void populateForm(Hike hike) {
        binding.editTextName.setText(hike.getName());
        binding.editTextLocation.setText(hike.getLocation());
        binding.editTextDate.setText(hike.getDate());
        selectedDate = hike.getDate();

        binding.radioGroupParking.check(
                hike.isParkingAvailable() ? R.id.radioParkingYes : R.id.radioParkingNo);

        binding.editTextLength.setText(String.valueOf(hike.getLengthKm()));

        // Set spinner selection
        String[] levels = {
                Hike.DIFFICULTY_EASY, Hike.DIFFICULTY_MODERATE,
                Hike.DIFFICULTY_HARD, Hike.DIFFICULTY_EXPERT
        };
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(hike.getDifficulty())) {
                binding.spinnerDifficulty.setSelection(i);
                break;
            }
        }

        binding.editTextDescription.setText(hike.getDescription());
        binding.editTextCustomField1.setText(hike.getCustomField1());
        binding.editTextCustomField2.setText(hike.getCustomField2());
    }

    // -------------------------------------------------------------------------
    // Form data collection
    // -------------------------------------------------------------------------

    /**
     * Reads all form fields and constructs a {@link Hike} POJO.
     * This does NOT validate — call {@link ValidationUtils#validateHike} separately.
     *
     * @return A {@link Hike} populated with the current UI state.
     */
    private Hike collectFormValues() {
        Hike hike = (existingHike != null) ? existingHike : new Hike();

        hike.setName(getText(binding.editTextName));
        hike.setLocation(getText(binding.editTextLocation));
        hike.setDate(selectedDate);

        int radioId = binding.radioGroupParking.getCheckedRadioButtonId();
        hike.setParkingAvailable(radioId == R.id.radioParkingYes);

        // Parse length safely – validation will catch non-numeric values.
        try {
            hike.setLengthKm(Double.parseDouble(getText(binding.editTextLength)));
        } catch (NumberFormatException e) {
            hike.setLengthKm(-1); // Sentinel: will fail validation
        }

        hike.setDifficulty(binding.spinnerDifficulty.getSelectedItem().toString());
        hike.setDescription(getText(binding.editTextDescription));
        hike.setCustomField1(getText(binding.editTextCustomField1));
        hike.setCustomField2(getText(binding.editTextCustomField2));

        return hike;
    }

    /** @return Trimmed text from an EditText, or empty string if null. */
    private String getText(android.widget.EditText editText) {
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }

    // -------------------------------------------------------------------------
    // Validation error display
    // -------------------------------------------------------------------------

    /**
     * Applies per-field error messages from a {@link ValidationResult} to the
     * corresponding {@code TextInputLayout} error labels.
     * Also clears errors on fields that passed.
     *
     * @param result The result returned by {@link ValidationUtils#validateHike}.
     */
    private void applyValidationErrors(ValidationResult result) {
        setFieldError(binding.inputLayoutName,       result.getError(ValidationUtils.FIELD_NAME));
        setFieldError(binding.inputLayoutLocation,   result.getError(ValidationUtils.FIELD_LOCATION));
        setFieldError(binding.inputLayoutDate,       result.getError(ValidationUtils.FIELD_DATE));
        setFieldError(binding.inputLayoutLength,     result.getError(ValidationUtils.FIELD_LENGTH));
        // Difficulty spinner: show Toast if invalid (spinner has no TextInputLayout wrapper).
        String diffError = result.getError(ValidationUtils.FIELD_DIFFICULTY);
        if (diffError != null) {
            Toast.makeText(this, diffError, Toast.LENGTH_SHORT).show();
        }
        // Scroll to first error for UX
        if (result.hasError(ValidationUtils.FIELD_NAME)) {
            binding.inputLayoutName.requestFocus();
        }
    }

    /** Sets or clears the error on a TextInputLayout. */
    private void setFieldError(com.google.android.material.textfield.TextInputLayout layout, String error) {
        layout.setError(error); // Passing null clears the error
    }

    // -------------------------------------------------------------------------
    // Summary confirmation dialog
    // -------------------------------------------------------------------------

    /**
     * Shows a summary dialog listing all entered values so the user can confirm
     * before the hike is persisted to the database.
     *
     * @param hike The fully-populated, validated hike object.
     */
    private void showSummaryDialog(Hike hike) {
        String summary = buildSummaryText(hike);
        String saveLabel = (existingHike != null)
                ? getString(R.string.action_update)
                : getString(R.string.action_save);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_summary_title)
                .setMessage(summary)
                .setPositiveButton(saveLabel, (d, w) -> persistHike(hike))
                .setNegativeButton(R.string.action_edit, null) // Dismisses – user goes back to form
                .setCancelable(true)
                .show();
    }

    /** Builds a human-readable summary string of all hike fields. */
    private String buildSummaryText(Hike hike) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(hike.getName()).append("\n");
        sb.append("Location: ").append(hike.getLocation()).append("\n");
        sb.append("Date: ").append(hike.getDate()).append("\n");
        sb.append("Parking: ").append(hike.isParkingAvailable() ? "Yes" : "No").append("\n");
        sb.append("Length: ").append(hike.getLengthKm()).append(" km\n");
        sb.append("Difficulty: ").append(hike.getDifficulty()).append("\n");

        if (hike.getDescription() != null && !hike.getDescription().isEmpty()) {
            sb.append("Description: ").append(hike.getDescription()).append("\n");
        }
        if (hike.getCustomField1() != null && !hike.getCustomField1().isEmpty()) {
            sb.append("Note 1: ").append(hike.getCustomField1()).append("\n");
        }
        if (hike.getCustomField2() != null && !hike.getCustomField2().isEmpty()) {
            sb.append("Note 2: ").append(hike.getCustomField2()).append("\n");
        }
        return sb.toString().trim();
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    /**
     * Dispatches the hike to the repository (add or update) and finishes this
     * Activity on success.
     *
     * @param hike The validated hike to persist.
     */
    private void persistHike(Hike hike) {
        binding.buttonPreviewSave.setEnabled(false);

        HikeRepository.OperationCallback callback = (success, message) -> {
            binding.buttonPreviewSave.setEnabled(true);
            if (success) {
                Toast.makeText(this, R.string.msg_hike_saved, Toast.LENGTH_SHORT).show();
                finish(); // Return to the list
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        };

        if (existingHike != null) {
            repository.updateHike(hike, callback);
        } else {
            repository.addHike(hike, callback);
        }
    }
}
