package com.example.m_hikeapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hikeapp.databinding.ActivitySearchFilterBinding;
import com.example.m_hikeapp.repository.HikeRepository;

/**
 * Advanced multi-criteria filter screen (Feature D – advanced search).
 *
 * <p>Allows filtering by location (substring), date range, and length range.
 * Results are passed back to the calling Activity via a shared data approach –
 * in this simple architecture the filter just starts a new
 * {@link HikeListActivity} with filter intent extras; you may adapt this to
 * use a ViewModel in a future refactor.</p>
 *
 * <p>For coursework simplicity, results are applied directly by reloading the
 * list in {@link HikeListActivity#onResume()} (the list always reloads), or
 * this Activity can pass filter params as Intent extras to a filtered list
 * view.  The implementation below shows filtered results in-place on this
 * screen using its own RecyclerView.</p>
 */
public class SearchFilterActivity extends AppCompatActivity {

    // -------------------------------------------------------------------------
    private ActivitySearchFilterBinding binding;
    private HikeRepository             repository;
    private com.example.m_hikeapp.adapter.HikeAdapter adapter;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding    = ActivitySearchFilterBinding.inflate(getLayoutInflater());
        repository = HikeRepository.getInstance(this);
        setContentView(binding.getRoot());

        setupToolbar();
        setupResultsList();
        setupFilterButton();
        setupClearButton();

        // Show all hikes initially
        applyFilter();
    }

    // -------------------------------------------------------------------------
    // Setup helpers
    // -------------------------------------------------------------------------

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_search_filter);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupResultsList() {
        adapter = new com.example.m_hikeapp.adapter.HikeAdapter(new com.example.m_hikeapp.adapter.HikeAdapter.HikeClickListener() {
            @Override
            public void onHikeClick(com.example.m_hikeapp.model.Hike hike) {
                android.content.Intent intent = new android.content.Intent(
                        SearchFilterActivity.this, HikeDetailActivity.class);
                intent.putExtra(HikeListActivity.EXTRA_HIKE_ID, hike.getId());
                startActivity(intent);
            }

            @Override
            public void onHikeDelete(com.example.m_hikeapp.model.Hike hike) {
                // Deletion not available from the filter screen to keep scope simple.
                Toast.makeText(SearchFilterActivity.this,
                        R.string.msg_delete_from_list, Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerFilterResults.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.recyclerFilterResults.setAdapter(adapter);
    }

    private void setupFilterButton() {
        binding.buttonApplyFilter.setOnClickListener(v -> applyFilter());
    }

    private void setupClearButton() {
        binding.buttonClearFilter.setOnClickListener(v -> {
            binding.editTextFilterLocation.setText("");
            binding.editTextFilterDateFrom.setText("");
            binding.editTextFilterDateTo.setText("");
            binding.editTextFilterMinLength.setText("");
            binding.editTextFilterMaxLength.setText("");
            applyFilter();
        });
    }

    // -------------------------------------------------------------------------
    // Filter logic
    // -------------------------------------------------------------------------

    /**
     * Reads filter inputs and delegates to {@link HikeRepository#filterHikes}.
     * All fields are optional – empty strings are treated as "no filter".
     */
    private void applyFilter() {
        String location  = getInputText(binding.editTextFilterLocation);
        String dateFrom  = getInputText(binding.editTextFilterDateFrom);
        String dateTo    = getInputText(binding.editTextFilterDateTo);
        String minStr    = getInputText(binding.editTextFilterMinLength);
        String maxStr    = getInputText(binding.editTextFilterMaxLength);

        Double minLength = parseDoubleOrNull(minStr);
        Double maxLength = parseDoubleOrNull(maxStr);

        // Basic input sanity: show inline hint if date format is wrong.
        if (!dateFrom.isEmpty() && !isValidDateFormat(dateFrom)) {
            binding.inputLayoutFilterDateFrom.setError(getString(R.string.error_date_format));
            return;
        }
        if (!dateTo.isEmpty() && !isValidDateFormat(dateTo)) {
            binding.inputLayoutFilterDateTo.setError(getString(R.string.error_date_format));
            return;
        }
        binding.inputLayoutFilterDateFrom.setError(null);
        binding.inputLayoutFilterDateTo.setError(null);

        repository.filterHikes(
                location.isEmpty() ? null : location,
                dateFrom.isEmpty() ? null : dateFrom,
                dateTo.isEmpty()   ? null : dateTo,
                minLength,
                maxLength,
                hikes -> {
                    adapter.submitList(hikes);
                    binding.textFilterResultCount.setText(
                            getString(R.string.label_filter_results, hikes.size()));
                    binding.textFilterEmpty.setVisibility(
                            hikes.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getInputText(android.widget.EditText editText) {
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private boolean isValidDateFormat(String date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        sdf.setLenient(false);
        try { sdf.parse(date); return true; } catch (java.text.ParseException e) { return false; }
    }
}
