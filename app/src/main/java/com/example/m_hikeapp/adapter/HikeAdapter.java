package com.example.m_hikeapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hikeapp.databinding.ItemHikeBinding;
import com.example.m_hikeapp.model.Hike;

/**
 * {@link RecyclerView} adapter for displaying a list of {@link Hike} entries.
 *
 * <p>Uses {@link ListAdapter} with {@link DiffUtil} for efficient, animated list
 * updates without full dataset redraws. Callers should call
 * {@link #submitList(java.util.List)} to push new data.</p>
 *
 * <p>Interaction events are surfaced via the {@link HikeClickListener} interface,
 * keeping the adapter free of navigation or business logic.</p>
 */
public class HikeAdapter extends ListAdapter<Hike, HikeAdapter.HikeViewHolder> {

    // -------------------------------------------------------------------------
    // Listener interface
    // -------------------------------------------------------------------------

    /** Callback for item-level interactions. */
    public interface HikeClickListener {
        /** Called when the user taps the hike card body. */
        void onHikeClick(Hike hike);
        /** Called when the user taps the delete icon on the card. */
        void onHikeDelete(Hike hike);
    }

    // -------------------------------------------------------------------------
    // DiffUtil callback
    // -------------------------------------------------------------------------

    private static final DiffUtil.ItemCallback<Hike> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Hike>() {
                @Override
                public boolean areItemsTheSame(@NonNull Hike oldItem, @NonNull Hike newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Hike oldItem, @NonNull Hike newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getDate().equals(newItem.getDate())
                            && oldItem.getDifficulty().equals(newItem.getDifficulty());
                }
            };

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private final HikeClickListener listener;

    public HikeAdapter(HikeClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // -------------------------------------------------------------------------
    // RecyclerView.Adapter overrides
    // -------------------------------------------------------------------------

    @NonNull
    @Override
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHikeBinding binding = ItemHikeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HikeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    // =========================================================================
    // ViewHolder
    // =========================================================================

    /** ViewHolder using ViewBinding to reference list item views. */
    static class HikeViewHolder extends RecyclerView.ViewHolder {

        private final ItemHikeBinding binding;

        HikeViewHolder(ItemHikeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds a {@link Hike} to this ViewHolder's views.
         *
         * @param hike     The data object.
         * @param listener Click/delete callback.
         */
        void bind(Hike hike, HikeClickListener listener) {
            binding.textHikeName.setText(hike.getName());
            binding.textHikeLocation.setText(hike.getLocation());
            binding.textHikeDate.setText(hike.getDate());
            binding.textHikeDifficulty.setText(hike.getDifficulty());
            binding.textHikeLength.setText(
                    String.format(java.util.Locale.getDefault(), "%.1f km", hike.getLengthKm()));

            // Parking status badge
            binding.textParkingStatus.setText(hike.isParkingAvailable() ? "P" : "No P");

            // Click on whole card -> detail view
            binding.getRoot().setOnClickListener(v -> listener.onHikeClick(hike));
            // Delete icon
            binding.buttonDeleteHike.setOnClickListener(v -> listener.onHikeDelete(hike));
        }
    }
}
