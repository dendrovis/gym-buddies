package sg.edu.ntu.scse.cz2006.gymbuddies.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewInteractedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.ViewHelper;


/**
 * Recycler Adapter for Buddy search result
 * For sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 *
 * @author Chia Yu
 * @since 2019-09-28
 * @property listBuddies List<User> The list of all searched users
 * @constructor Creates a adapter for the Buddy Search Result List RecyclerView
 */
public class BuddyResultAdapter extends RecyclerView.Adapter<BuddyResultAdapter.ViewHolder> {
    /**
     * TAG is unique identifier to for logging purpose
     */
    private String TAG = "GB.Adapter.BuddyResult";
    /**
     * unique identifier to specify undefined action
     * @see #listener
     */
    public static final int ACTION_INVALID = -1;
    /**
     * unique identifier to specify user click on item body of display view
     * @see #listener
     */
    public static final int ACTION_CLICK_ON_ITEM_BODY   = 1;
    /**
     * unique identifier to specify user click on favoured (heart shape) button of display view
     * @see #listener
     */
    public static final int ACTION_CLICK_ON_FAV_ITEM    = 2;
    /**
     * unique identifier to specify user click on profile picture of display view
     * @see #listener
     */
    public static final int ACTION_CLICK_ON_ITEM_PIC = 3;
    /**
     * list that holds user data for displaying
     */
    private List<User> listBuddies;
    /**
     * interface that allow activity to observe user interaction with RecyclerView
     */
    private OnRecyclerViewInteractedListener<ViewHolder> listener;
    /**
     * it store list of other user's id, used to denote that other user is current user's buddy
     * @see sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavBuddyRecord
     */
    private List<String> favUsers;


    /**
     * Constructor to create Recycler Adapter for Buddy search result
     * @param listBuddies
     * @param favUsers
     */
    public BuddyResultAdapter(List<User> listBuddies, List<String> favUsers) {
        this.listBuddies = listBuddies;
        this.favUsers = favUsers;
    }


    /**
     * Allow other classes to listen to user interaction via OnBuddyClickedListener
     */
    public void setOnRecyclerViewClickedListener( OnRecyclerViewInteractedListener<ViewHolder> listener){
        this.listener=listener;
    }

    /**
     * get number of items to be display on recycler view
     */
    @Override
    public int getItemCount() {
        return this.listBuddies.size();
    }

    /**
     * render an item view, and assign the view to view holder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.row_buddy, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }


    /**
     * get number of items to be display on recycler view
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.update();
    }


    /**
     * View Holder to hold references to frequent modified views
     *
     * @author Chia Yu
     * @since 2019-09-28
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        /**
         * view that display user name
         */
        TextView tvName;
        /**
         * view that display user's profile picture
         */
        ImageView imgViewPic;
        /**
         * view that display user's gender by icon
         */
        ImageView imgViewGender;
        /**
         * view group that display user's preferred workout days
         */
        LinearLayout llPrefDays;
        /**
         * view that denote whether a user is buddy of current user
         */
        CheckBox cbFav;

        /**
         * constructor method
         * @param itemView view that represents the whole item
         */
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_bd_name);
            imgViewPic = itemView.findViewById(R.id.img_bd_pic);
            imgViewGender = itemView.findViewById(R.id.img_bd_gender);
            llPrefDays = itemView.findViewById(R.id.ll_pref_days);
            cbFav = itemView.findViewById(R.id.cb_bd_fav);

            // set up click listener
            super.itemView.setOnClickListener(this);
            imgViewPic.setOnClickListener(this);
            cbFav.setOnClickListener(this);


            // programmingly change profdays
            CheckBox cbDay;
            final float scale = itemView.getContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (36 * scale + 0.5f);
            llPrefDays.getLayoutParams().height = pixels;
            for (int i = 0; i < llPrefDays.getChildCount(); i++) {
                cbDay = (CheckBox) llPrefDays.getChildAt(i);
                cbDay.setText( cbDay.getText().subSequence(0, 1));
                cbDay.setEnabled(false);
                cbDay.setClickable(false);
            }
        }

        /**
         * update item view based on holder's current position
         */
        private void update(){
            User curUser = listBuddies.get(getAdapterPosition());
            tvName.setText(curUser.getName());
            if (curUser.getGender().equals("Male")) {
                imgViewGender.setImageResource(R.drawable.ic_human_male);
            } else {
                imgViewGender.setImageResource(R.drawable.ic_human_female);
            }

            cbFav.setOnClickListener(null);
            cbFav.setChecked(false);
            if (favUsers.contains(curUser.getUid()) ){
                cbFav.setChecked(true);
            }
            cbFav.setOnClickListener(this);

            updatePrefDays(curUser);
            ViewHelper.updateUserPic(imgViewPic, curUser);
        }


        /**
         * update preferred workout days based on user
         */
        private  void updatePrefDays(User user){
            ((CheckBox) llPrefDays.getChildAt(0)).setChecked(user.getPrefDay().getMonday());
            ((CheckBox) llPrefDays.getChildAt(1)).setChecked(user.getPrefDay().getTuesday());
            ((CheckBox) llPrefDays.getChildAt(2)).setChecked(user.getPrefDay().getWednesday());
            ((CheckBox) llPrefDays.getChildAt(3)).setChecked(user.getPrefDay().getThursday());
            ((CheckBox) llPrefDays.getChildAt(4)).setChecked(user.getPrefDay().getFriday());
            ((CheckBox) llPrefDays.getChildAt(5)).setChecked(user.getPrefDay().getSaturday());
            ((CheckBox) llPrefDays.getChildAt(6)).setChecked(user.getPrefDay().getSunday());
        }


        /**
         * when user click on certain view, it update observer by interface
         * @param view
         * @see #listener
         */
        @Override
        public void onClick(View view) {
            int action = ACTION_INVALID;
            if (view == super.itemView) {
                action = ACTION_CLICK_ON_ITEM_BODY;
            } else if (view == cbFav){
                action = ACTION_CLICK_ON_FAV_ITEM;
            } else if (view == imgViewPic){
                action = ACTION_CLICK_ON_ITEM_PIC;
            }
            if (action != ACTION_INVALID ) {
                if (listener != null){
                    listener.onViewInteracted(view, this, action);
                }
            }
        }
    }
}
