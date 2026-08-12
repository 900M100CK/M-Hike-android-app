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
import com.example.m_hikeapp.adapter.PublicHikeAdapter;
import com.example.m_hikeapp.databinding.ActivityHikeListBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;
import com.example.m_hikeapp.repository.HikeRepository;
import com.example.m_hikeapp.sync.FirebaseSyncHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        implements HikeAdapter.HikeClickListener, PublicHikeAdapter.PublicHikeClickListener {

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
    private PublicHikeAdapter       publicAdapter;
    private boolean                 isOnlineFeed = false;
    private List<Map<String, Object>> currentPublicHikes = new ArrayList<>();

    public static Hike selectedPublicHike;
    public static List<Observation> selectedPublicObservations;

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
        setupTabs();
        setupRecyclerView();
        setupSearchBar();
        setupFab();
        setupDeleteAllButton();
        setupAddSamplesButton();
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
        if (isOnlineFeed) {
            loadPublicFeed();
        } else {
            loadAllHikes();
        }
        loadLiveWeather();
    }

    private void loadLiveWeather() {
        com.example.m_hikeapp.util.WeatherHelper.fetchCurrentWeather(this, new com.example.m_hikeapp.util.WeatherHelper.WeatherCallback() {
            @Override
            public void onSuccess(String weatherInfo) {
                runOnUiThread(() -> binding.textLiveWeather.setText(weatherInfo));
            }

            @Override
            public void onFailure(String errorMsg) {
                runOnUiThread(() -> binding.textLiveWeather.setText("Unable to fetch weather"));
            }
        });
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

    private void setupTabs() {
        if (binding.tabLayout == null) return;
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isOnlineFeed = (tab.getPosition() == 1);
                binding.recyclerHikes.setAdapter(isOnlineFeed ? publicAdapter : adapter);

                int visibility = isOnlineFeed ? View.GONE : View.VISIBLE;
                binding.buttonFilter.setVisibility(visibility);
                binding.buttonAddSamples.setVisibility(visibility);
                binding.buttonDeleteAll.setVisibility(visibility);
                binding.fabAddHike.setVisibility(visibility);

                binding.editTextSearch.setText("");

                if (isOnlineFeed) {
                    loadPublicFeed();
                } else {
                    loadAllHikes();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new HikeAdapter(this);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        publicAdapter = new PublicHikeAdapter(uid, this);
        binding.recyclerHikes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHikes.setAdapter(adapter);
    }

    private void setupSearchBar() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int cnt, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (isOnlineFeed) {
                    if (query.isEmpty()) {
                        submitPublicList(currentPublicHikes);
                    } else {
                        String q = query.toLowerCase();
                        List<Map<String, Object>> filtered = new ArrayList<>();
                        for (Map<String, Object> map : currentPublicHikes) {
                            Hike h = (Hike) map.get("hike");
                            if (h != null && h.getName().toLowerCase().contains(q)) {
                                filtered.add(map);
                            }
                        }
                        submitPublicList(filtered);
                    }
                } else {
                    if (query.isEmpty()) {
                        loadAllHikes();
                    } else {
                        repository.searchHikesByName(query, HikeListActivity.this::submitList);
                    }
                }
            }
        });
    }

    private void setupFab() {
        binding.fabAddHike.setOnClickListener(v ->
                startActivity(new Intent(this, AddHikeActivity.class)));
    }

    private void setupDeleteAllButton() {
        binding.buttonDeleteAll.setOnClickListener(v -> confirmDeleteAll());
    }

    private void setupAddSamplesButton() {
        binding.buttonAddSamples.setOnClickListener(v -> {
            Toast.makeText(this, "Seeding sample data...", Toast.LENGTH_SHORT).show();
            com.example.m_hikeapp.util.DevSeedHelper.seedIfEmpty(this, this::loadAllHikes);
        });
    }

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

    private void submitList(List<Hike> hikes) {
        if (isOnlineFeed) return;
        adapter.submitList(hikes);
        boolean empty = hikes.isEmpty();
        binding.textEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.imgEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void loadPublicFeed() {
        FirebaseSyncHelper.getInstance().fetchPublicHikes(new FirebaseSyncHelper.PublicFetchCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> publicHikesData) {
                currentPublicHikes = publicHikesData;
                submitPublicList(publicHikesData);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HikeListActivity.this, "Failed to load public feed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitPublicList(List<Map<String, Object>> list) {
        if (!isOnlineFeed) return;
        publicAdapter.submitList(list);
        boolean empty = list.isEmpty();
        binding.textEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.imgEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
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
        repository.deleteAllHikes((success, message) -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            loadAllHikes();
        });
    }

    // -------------------------------------------------------------------------
    // HikeClickListener implementation (Local)
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

    @Override
    public void onHikePublish(Hike hike) {
        new AlertDialog.Builder(this)
            .setTitle("Publish Hike")
            .setMessage("Do you want to share '" + hike.getName() + "' to the online feed?")
            .setPositiveButton("Publish", (dialog, which) -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                repository.getObservationsForHike(hike.getId(), observations -> {
                    FirebaseSyncHelper.getInstance().publishPublicHike(
                        user.getUid(),
                        user.getEmail() != null ? user.getEmail() : "anonymous",
                        hike,
                        observations,
                        new FirebaseSyncHelper.PushCallback() {
                            @Override
                            public void onSuccess(Hike h) {
                                Toast.makeText(HikeListActivity.this, "Published!", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(Hike h, Exception e) {
                                Toast.makeText(HikeListActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    );
                });
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    // -------------------------------------------------------------------------
    // PublicHikeClickListener implementation (Online)
    // -------------------------------------------------------------------------

    @Override
    public void onHikeClick(Hike hike, List<Observation> observations) {
        selectedPublicHike = hike;
        selectedPublicObservations = observations;
        Intent intent = new Intent(this, HikeDetailActivity.class);
        intent.putExtra(EXTRA_HIKE_ID, hike.getId());
        intent.putExtra("IS_ONLINE", true);
        startActivity(intent);
    }

    @Override
    public void onHikeUnpublish(long hikeId, String authorUid) {
        new AlertDialog.Builder(this)
            .setTitle("Unpublish Hike")
            .setMessage("Remove this hike from the public feed?")
            .setPositiveButton("Remove", (dialog, which) -> {
                FirebaseSyncHelper.getInstance().removePublicHike(authorUid, hikeId);
                Toast.makeText(this, "Removed from feed", Toast.LENGTH_SHORT).show();
                loadPublicFeed();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }
}
