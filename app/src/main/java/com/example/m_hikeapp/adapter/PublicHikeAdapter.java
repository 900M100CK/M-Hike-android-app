package com.example.m_hikeapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hikeapp.databinding.ItemHikeBinding;
import com.example.m_hikeapp.model.Hike;
import com.example.m_hikeapp.model.Observation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PublicHikeAdapter extends RecyclerView.Adapter<PublicHikeAdapter.PublicHikeViewHolder> {

    public interface PublicHikeClickListener {
        void onHikeClick(Hike hike, List<Observation> observations);
        void onHikeUnpublish(long hikeId, String authorUid);
    }

    private final PublicHikeClickListener listener;
    private final String currentUserId;
    private List<Map<String, Object>> publicHikes = new ArrayList<>();

    public PublicHikeAdapter(String currentUserId, PublicHikeClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void submitList(List<Map<String, Object>> list) {
        this.publicHikes = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PublicHikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHikeBinding binding = ItemHikeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PublicHikeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicHikeViewHolder holder, int position) {
        holder.bind(publicHikes.get(position), currentUserId, listener);
    }

    @Override
    public int getItemCount() {
        return publicHikes.size();
    }

    static class PublicHikeViewHolder extends RecyclerView.ViewHolder {
        private final ItemHikeBinding binding;

        PublicHikeViewHolder(ItemHikeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Map<String, Object> data, String currentUserId, PublicHikeClickListener listener) {
            Hike hike = (Hike) data.get("hike");
            String authorUid = (String) data.get("authorUid");
            String authorEmail = (String) data.get("authorEmail");
            List<Observation> obsList = (List<Observation>) data.get("observations");

            binding.textHikeName.setText(hike.getName());
            binding.textHikeLocation.setText(hike.getLocation());
            binding.textHikeDate.setText(hike.getDate());
            binding.textHikeDifficulty.setText(hike.getDifficulty());
            binding.textHikeLength.setText(String.format(java.util.Locale.getDefault(), "%.1f km", hike.getLengthKm()));

            binding.textParkingStatus.setText(hike.isParkingAvailable() ? "P" : "No");

            if (hike.getPhotoUri() != null && !hike.getPhotoUri().isEmpty()) {
                com.example.m_hikeapp.util.ImageUriUtils.loadImage(binding.getRoot().getContext(), binding.imageHikeCover, hike.getPhotoUri());
                binding.imageHikeCover.setPadding(0, 0, 0, 0);
            } else {
                binding.imageHikeCover.setImageResource(com.example.m_hikeapp.R.drawable.ic_hiking);
                int pad = (int) (12 * binding.getRoot().getContext().getResources().getDisplayMetrics().density);
                binding.imageHikeCover.setPadding(pad, pad, pad, pad);
            }

            binding.textHikeAuthor.setVisibility(View.VISIBLE);
            binding.textHikeAuthor.setText("By: " + authorEmail);

            binding.buttonPublishHike.setVisibility(View.GONE);

            if (currentUserId != null && currentUserId.equals(authorUid)) {
                binding.buttonDeleteHike.setVisibility(View.VISIBLE);
                binding.buttonDeleteHike.setOnClickListener(v -> listener.onHikeUnpublish(hike.getId(), authorUid));
            } else {
                binding.buttonDeleteHike.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> listener.onHikeClick(hike, obsList));
        }
    }
}
