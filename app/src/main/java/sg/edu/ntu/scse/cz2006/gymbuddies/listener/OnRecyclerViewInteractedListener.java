package sg.edu.ntu.scse.cz2006.gymbuddies.listener;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * The interface allow RecyclerView.Adapter to send notify observer of view interactions
 *
 * @author Chia Yu
 * @since 2019-10-22
 */
public interface OnRecyclerViewInteractedListener<T extends RecyclerView.ViewHolder> {
    /**
     * interface for subject to sent notification and for observer to implementation
     * @param view
     * @param holder
     * @param action
     */
    void onViewInteracted(View view, T holder, int action);
}
