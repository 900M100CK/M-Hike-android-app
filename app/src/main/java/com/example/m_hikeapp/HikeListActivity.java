package com.example.m_hikeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.m_hikeapp.adapter.HikeAdapter;
import com.example.m_hikeapp.databinding.ActivityHikeListBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.repository.HikeRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Main screen: displays all saved hikes in a {@link androidx.recyclerview.widget.RecyclerView},
 * provides a search bar (Feature D – basic search) and navigation to add/filter/detail screens.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Observe hike list changes and refresh the adapter.</li>
 *   <li>Delegate all DB operations to {@link HikeRepository}.</li>
 *   <li>Show confirmation dialog before delete-all.</li>
 * </ul>
 */
public class HikeListActivity extends AppCompatActivity
        implements HikeAdapter.HikeClickListener {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    public static final String EXTRA_HIKE_ID = "extra_hike_id";

    // -------------------------------------------------------------------------
    // ViewBinding & dependencies
    // -------------------------------------------------------------------------
    private ActivityHikeListBinding binding;
    private HikeRepository          repository;
    private HikeAdapter             adapter;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding    = ActivityHikeListBinding.inflate(getLayoutInflater());
        repository = HikeRepository.getInstance(this);
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupSearchBar();
        setupFab();
        setupDeleteAllButton();
        setupFilterButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        // Reload data whenever we return from Add/Edit/Detail screens.
        loadAllHikes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hike_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // -------------------------------------------------------------------------
    // Setup helpers
    // -------------------------------------------------------------------------

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void setupRecyclerView() {
        adapter = new HikeAdapter(this);
        binding.recyclerHikes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHikes.setAdapter(adapter);
    }

    /**
     * Wires the search input to trigger a live name-search after each keystroke.
     * An empty query reloads the full list.
     */
    private void setupSearchBar() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int cnt, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadAllHikes();
                } else {
                    repository.searchHikesByName(query, HikeListActivity.this::submitList);
                }
            }
        });
    }

    /** FAB navigates to {@link AddHikeActivity} to create a new hike. */
    private void setupFab() {
        binding.fabAddHike.setOnClickListener(v ->
                startActivity(new Intent(this, AddHikeActivity.class)));
    }

    /** "Delete All" overflow/toolbar action with confirmation dialog. */
    private void setupDeleteAllButton() {
        binding.buttonDeleteAll.setOnClickListener(v -> confirmDeleteAll());
    }

    /** Filter button navigates to {@link SearchFilterActivity}. */
    private void setupFilterButton() {
        binding.buttonFilter.setOnClickListener(v ->
                startActivity(new Intent(this, SearchFilterActivity.class)));
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    private void loadAllHikes() {
        repository.getAllHikes(this::submitList);
    }

    /**
     * Submits a new list to the adapter and toggles the empty-state view.
     *
     * @param hikes The hike list to display.
     */
    private void submitList(List<Hike> hikes) {
        adapter.submitList(hikes);
        binding.textEmptyState.setVisibility(hikes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // -------------------------------------------------------------------------
    // Delete all
    // -------------------------------------------------------------------------

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_all_title)
                .setMessage(R.string.dialog_delete_all_message)
                .setPositiveButton(R.string.action_delete_all, (d, w) -> deleteAllHikes())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteAllHikes() {
        repository.deleteAllHikes((success, message) ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
        // onResume() will refresh the list automatically.
    }

    // -------------------------------------------------------------------------
    // HikeClickListener implementation
    // -------------------------------------------------------------------------

    @Override
    public void onHikeClick(Hike hike) {
        Intent intent = new Intent(this, HikeDetailActivity.class);
        intent.putExtra(EXTRA_HIKE_ID, hike.getId());
        startActivity(intent);
    }

    @Override
    public void onHikeDelete(Hike hike) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_hike_title)
                .setMessage(getString(R.string.dialog_delete_hike_message, hike.getName()))
                .setPositiveButton(R.string.action_delete, (d, w) ->
                        repository.deleteHike(hike.getId(), (success, msg) -> {
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            loadAllHikes();
                        }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
