package com.example.m_hikeapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hikeapp.databinding.ItemObservationBinding;
import com.example.m_hikeapp.model.Observation;

/**
 * {@link RecyclerView} adapter for displaying {@link Observation} items
 * within the hike detail screen.
 *
 * <p>Mirrors the pattern used in {@link HikeAdapter}: uses {@link ListAdapter}
 * with {@link DiffUtil} and exposes a {@link ObservationClickListener} for
 * interaction events.</p>
 */
public class ObservationAdapter extends ListAdapter<Observation, ObservationAdapter.ObsViewHolder> {

    // -------------------------------------------------------------------------
    // Listener interface
    // -------------------------------------------------------------------------

    /** Callback for observation item interactions. */
    public interface ObservationClickListener {
        void onEditObservation(Observation observation);
        void onDeleteObservation(Observation observation);
    }

    // -------------------------------------------------------------------------
    // DiffUtil
    // -------------------------------------------------------------------------

    private static final DiffUtil.ItemCallback<Observation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Observation>() {
                @Override
                public boolean areItemsTheSame(@NonNull Observation o, @NonNull Observation n) {
                    return o.getId() == n.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Observation o, @NonNull Observation n) {
                    return o.getTitle().equals(n.getTitle())
                            && o.getObsTime().equals(n.getObsTime());
                }
            };

    // -------------------------------------------------------------------------
    private final ObservationClickListener listener;

    public ObservationAdapter(ObservationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ObsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemObservationBinding binding = ItemObservationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ObsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ObsViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    // =========================================================================
    static class ObsViewHolder extends RecyclerView.ViewHolder {

        private final ItemObservationBinding binding;

        ObsViewHolder(ItemObservationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Observation obs, ObservationClickListener listener) {
            binding.textObsTitle.setText(obs.getTitle());
            binding.textObsTime.setText(obs.getObsTime());

            // Details badge (Temp + Step count)
            StringBuilder badgeText = new StringBuilder();
            if (obs.getTemperatureCelsius() != null) {
                badgeText.append("🌡 ").append(String.format(java.util.Locale.getDefault(), "%.1f°C", obs.getTemperatureCelsius()));
            }
            if (obs.getStepCount() != null && obs.getStepCount() > 0) {
                if (badgeText.length() > 0) badgeText.append("  •  ");
                badgeText.append("👣 ").append(String.format(java.util.Locale.getDefault(), "%,d steps", obs.getStepCount()));
            }
            binding.textObsBadgeDetails.setText(badgeText.toString());
            binding.textObsBadgeDetails.setVisibility(badgeText.length() > 0 ? android.view.View.VISIBLE : android.view.View.GONE);

            // Photo preview
            if (obs.getPhotoUri() != null && !obs.getPhotoUri().isEmpty()) {
                binding.imageObsItemPhoto.setImageURI(android.net.Uri.parse(obs.getPhotoUri()));
                binding.imageObsItemPhoto.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.imageObsItemPhoto.setVisibility(android.view.View.GONE);
            }

            String comment = obs.getComment();
            binding.textObsComment.setText(
                    (comment != null && !comment.isEmpty()) ? comment : "—");

            binding.buttonEditObs.setOnClickListener(v -> listener.onEditObservation(obs));
            binding.buttonDeleteObs.setOnClickListener(v -> listener.onDeleteObservation(obs));
        }
    }
}
