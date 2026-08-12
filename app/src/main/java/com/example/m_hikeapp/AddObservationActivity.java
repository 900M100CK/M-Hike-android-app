package com.example.m_hikeapp;

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

    private String capturedPhotoUriStr = null;
    private android.net.Uri currentCaptureUri = null;
    private boolean isUploadingPhoto = false;

    private final androidx.activity.result.ActivityResultLauncher<android.net.Uri> capturePhotoLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && currentCaptureUri != null) {
                    capturedPhotoUriStr = currentCaptureUri.toString();
                    com.example.m_hikeapp.util.ImageUriUtils.loadImage(this, binding.imageObsPhotoPreview, capturedPhotoUriStr);
                    uploadPhotoToImgBb(capturedPhotoUriStr);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> selectPhotoLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    capturedPhotoUriStr = uri.toString();
                    com.example.m_hikeapp.util.ImageUriUtils.loadImage(this, binding.imageObsPhotoPreview, capturedPhotoUriStr);
                    uploadPhotoToImgBb(capturedPhotoUriStr);
                }
            });

    /**
     * Feature: Voice-to-Text for comments (Fix for GSA onError 65561).
     * Using RecognizerIntent for a robust system-handled UI experience.
     */
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> voiceInputLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    java.util.ArrayList<String> matches = result.getData().getStringArrayListExtra(
                            android.speech.RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        android.text.Editable currentEditable = binding.editTextObsComment.getText();
                        String currentText = (currentEditable != null) ? currentEditable.toString() : "";
                        if (!currentText.isEmpty()) {
                            binding.editTextObsComment.setText(currentText + " " + spokenText);
                        } else {
                            binding.editTextObsComment.setText(spokenText);
                        }
                        binding.editTextObsComment.setSelection(binding.editTextObsComment.getText().length());
                    }
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<String> requestAudioPermissionLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startVoiceInput();
                } else {
                    Toast.makeText(this, "Audio permission is required for voice input", Toast.LENGTH_SHORT).show();
                }
            });

    private void startVoiceInput() {
        android.content.Intent intent = new android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault());
        intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now to add to your comment...");
        try {
            voiceInputLauncher.launch(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhotoToImgBb(String uriStr) {
        isUploadingPhoto = true;
        binding.buttonSaveObservation.setEnabled(false);
        android.widget.Toast.makeText(this, "Uploading photo to ImgBB...", android.widget.Toast.LENGTH_SHORT).show();
        com.example.m_hikeapp.util.ImgBbHelper.uploadImage(this, uriStr, new com.example.m_hikeapp.util.ImgBbHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                isUploadingPhoto = false;
                binding.buttonSaveObservation.setEnabled(true);
                capturedPhotoUriStr = imageUrl;
                com.example.m_hikeapp.util.ImageUriUtils.loadImage(AddObservationActivity.this, binding.imageObsPhotoPreview, imageUrl);
                android.widget.Toast.makeText(AddObservationActivity.this, "Photo uploaded to ImgBB!", android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                isUploadingPhoto = false;
                binding.buttonSaveObservation.setEnabled(true);
                android.util.Log.w("AddObservationActivity", "ImgBB upload failed: " + e.getMessage());
            }
        });
    }

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
        lockTimeToNow();     // G4: obs_time is auto-set to current time, not editable
        setupPhotoButton();
        setupVoiceButton();
        setupSaveButton();

        long observationId = getIntent().getLongExtra(HikeDetailActivity.EXTRA_OBSERVATION_ID, -1L);
        if (observationId != -1L) {
            loadObservationForEditing(observationId);
        } else {
            // G4: Fetch current + next-hour temperature from Open-Meteo on create mode
            com.example.m_hikeapp.util.WeatherHelper.fetchDetailedWeather(this,
                    new com.example.m_hikeapp.util.WeatherHelper.DetailedWeatherCallback() {
                @Override
                public void onSuccess(double currentTemp, double nextHourTemp,
                                      String condition, String forecastWarning) {
                    runOnUiThread(() -> {
                        // Pre-fill with current temperature (what hiker is experiencing now)
                        if (binding.editTextObsTemp.getText().toString().isEmpty()) {
                            binding.editTextObsTemp.setText(
                                    String.format(Locale.getDefault(), "%.1f", currentTemp));
                        }
                        // Show both current and next-hour prediction as a helper label
                        String tempHint = String.format(Locale.getDefault(),
                                "Now: %.1f°C  •  In 1h: %.1f°C", currentTemp, nextHourTemp);
                        binding.textViewWeatherHint.setText(tempHint);
                        binding.textViewWeatherHint.setVisibility(android.view.View.VISIBLE);

                        // Show warning toast if rain/snow/storm expected next hour
                        if (!forecastWarning.isEmpty()) {
                            Toast.makeText(AddObservationActivity.this,
                                    forecastWarning, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(String errorMsg) {
                    // Silently ignore — temperature field stays empty for manual entry
                }
            });
        }
    }

    private void setupPhotoButton() {
        binding.buttonPickPhoto.setOnClickListener(v -> selectPhotoLauncher.launch("image/*"));
        binding.buttonCapturePhoto.setOnClickListener(v -> {
            try {
                java.io.File photoFile = com.example.m_hikeapp.util.ImageUriUtils.createPhotoFile(this);
                currentCaptureUri = com.example.m_hikeapp.util.ImageUriUtils.toContentUri(this, photoFile);
                capturePhotoLauncher.launch(currentCaptureUri);
            } catch (java.io.IOException e) {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupVoiceButton() {
        binding.inputLayoutObsComment.setEndIconOnClickListener(v -> {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            } else {
                requestAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
            }
        });
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

    /**
     * Locks obs_time to the current clock time and makes the field read-only.
     *
     * <p>Observations record <em>what is happening right now</em>, so the time
     * should reflect the exact moment the user taps "Add Observation" — not an
     * arbitrary time the user might accidentally change.</p>
     */
    private void lockTimeToNow() {
        Calendar now = Calendar.getInstance();
        selectedTime = String.format(Locale.US, "%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        binding.editTextObsTime.setText(selectedTime);
        // Make completely non-interactive — no cursor, no click, no end icon
        binding.editTextObsTime.setFocusable(false);
        binding.editTextObsTime.setClickable(false);
        binding.editTextObsTime.setCursorVisible(false);
        binding.inputLayoutObsTime.setEndIconMode(
                com.google.android.material.textfield.TextInputLayout.END_ICON_NONE);
        binding.inputLayoutObsTime.setHint(
                getString(R.string.hint_obs_time_auto));
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
        binding.buttonSaveObservation.setEnabled(false);
        repository.getObservationsForHike(hikeId, observations -> {
            binding.buttonSaveObservation.setEnabled(true);
            Observation found = null;
            for (Observation obs : observations) {
                if (obs.getId() == observationId) {
                    found = obs;
                    break;
                }
            }
            if (found == null) {
                Toast.makeText(this, R.string.error_observation_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            existingObservation = found;
            populateForm(found);
        });
    }

    private void populateForm(Observation obs) {
        binding.editTextObsTitle.setText(obs.getTitle());
        binding.editTextObsTime.setText(obs.getObsTime());
        selectedTime = obs.getObsTime();
        binding.editTextObsComment.setText(obs.getComment());

        if (obs.getStepCount() != null) {
            binding.editTextObsStepCount.setText(String.valueOf(obs.getStepCount()));
        }
        if (obs.getTemperatureCelsius() != null) {
            binding.editTextObsTemp.setText(String.valueOf(obs.getTemperatureCelsius()));
        }
        if (obs.getPhotoUri() != null && !obs.getPhotoUri().isEmpty()) {
            capturedPhotoUriStr = obs.getPhotoUri();
            com.example.m_hikeapp.util.ImageUriUtils.loadImage(this, binding.imageObsPhotoPreview, obs.getPhotoUri());
        }
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

        String stepsStr = getText(binding.editTextObsStepCount);
        if (!stepsStr.isEmpty()) {
            try {
                obs.setStepCount(Integer.parseInt(stepsStr));
            } catch (NumberFormatException ignored) {}
        } else {
            obs.setStepCount(null);
        }

        String tempStr = getText(binding.editTextObsTemp);
        if (!tempStr.isEmpty()) {
            try {
                obs.setTemperatureCelsius(Double.parseDouble(tempStr));
            } catch (NumberFormatException ignored) {}
        } else {
            obs.setTemperatureCelsius(null);
        }

        obs.setPhotoUri(capturedPhotoUriStr);
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
