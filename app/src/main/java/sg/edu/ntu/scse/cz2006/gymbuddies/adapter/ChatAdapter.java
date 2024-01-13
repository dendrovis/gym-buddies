package sg.edu.ntu.scse.cz2006.gymbuddies.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Chat;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewInteractedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.ViewHelper;

/**
 *  Recycler Adapter for Chat Object
 *  For sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 *
 *  The adapter is used to display list of chat history of a user
 *
 * @author Chia Yu
 * @since 2019-10-19
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    /**
     * TAG is unique identifier to for logging purpose
     */
    private String TAG = "gb.adapter.chatlist";
    /**
     * unique identifier to specify undefined action
     * @see #listener
     */
    public static final int ACTION_INVALID = -1;
    /**
     * unique identifier to specify user click on item body of display view
     * @see #listener
     */
    public static final int ACTION_CLICK_ON_ITEM_BODY = 1;
    /**
     * unique identifier to specify user click on favoured (heart shape) button of display view
     * @see #listener
     */
    public static final int ACTION_CLICK_ON_FAV_ITEM = 2;
    /**
     * unique identifier to specify user click on profile picture of display view
     * @see #listener
     */
    public static final int ACTION_CLICK_ON_ITEM_PIC = 3;
    /**
     * it is used to format timestamp from long to String
     */
    private SimpleDateFormat sdf;
    /**
     * list that holds user's chat chat information
     * @see Chat
     */
    private List<Chat> chats;
    /**
     * it store list of other user's id, used to denote that other user is current user's buddy
     * @see sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavBuddyRecord
     */
    private List<String> favUserIds;
    /**
     * interface that allow activity to observe user interaction with RecyclerView
     */
    private OnRecyclerViewInteractedListener<ChatViewHolder> listener;

    /**
     * Constructor method to initialise ChatAdapter
     *
     * @param chats
     * @param favUserIds
     */
    public ChatAdapter(List<Chat> chats, List<String> favUserIds) {
        this.chats = chats;
        this.favUserIds = favUserIds;
        this.sdf = new SimpleDateFormat("dd/MM/yy");
    }

    /**
     * The method provides an interface to allow other class to register itself as observer of user interacting event
     *
     * @param listener
     * @see OnRecyclerViewInteractedListener
     */
    public void setOnRecyclerViewClickedListener(OnRecyclerViewInteractedListener<ChatViewHolder> listener) {
        this.listener = listener;
    }


    /**
     * setter method to update list of favouried
     * @param favUserIds
     */
    public void setFavUserIds(List<String> favUserIds) {
        this.favUserIds = favUserIds;
        notifyDataSetChanged();
    }


    /**
     * the method is return number of messages to be display on RecyclerView
     */
    @Override
    public int getItemCount() {
        return this.chats.size();
    }

    /**
     * the method based on view type to render a view and associate the view with respective view holder
     * @param parent view group to hold current item view
     * @param viewType type of layout to be rendered for current view
     * @return ViewHolder that associated with created view
     * @see #getItemViewType(int)
     */
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat, parent, false);

        return new ChatViewHolder(view);
    }

    /**
     * this method make use of view holder to update display information onto the view
     *
     * @param holder view holder of updating view
     * @param position position of current view
     * @see #getItemViewType(int)
     */
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat curChat = chats.get(position);
        holder.bind(curChat);
    }


    /**
     * ViewHolder for {@link Chat}
     * it holds reference to frequent update views and provides helper method to bind data information onto views
     */
    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        /**
         * name of the chat, usually display name of another user involved in chat
         */
        TextView tvName;
        /**
         * last message of the chat
         */
        TextView tvLastMsg;
        /**
         * formatted timestamp for chat's last update time
         */
        TextView tvUpdateTime;
        /**
         * icon for chat, usually display other user's profile picture
         */
        ImageView imgPic;
        /**
         * CheckBox button that denote the another user involved is buddy of current user
         */
        CheckBox cbFav;

        /**
         * constructor method that initialised the view holder by keeping reference to most frequent update views
         * @param itemView
         */
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_bd_name);
            tvLastMsg = itemView.findViewById(R.id.tv_last_msg);
            tvUpdateTime = itemView.findViewById(R.id.tv_last_msg_date);
            imgPic = itemView.findViewById(R.id.img_bd_pic);
            cbFav = itemView.findViewById(R.id.cb_bd_fav);

            itemView.setOnClickListener(this);
            imgPic.setOnClickListener(this);
            cbFav.setOnClickListener(this);
        }

        /**
         * helper method to bind {@link Chat} onto item view
         * @param chat
         */
        public void bind(Chat chat) {
            imgPic.setImageResource(R.mipmap.ic_launcher);
            tvName.setText("");
            // update fav
            cbFav.setOnClickListener(null);
            cbFav.setChecked(false);
            if (chat.getOtherUser() != null) {
                Log.d(TAG, "attempt update user");
                tvName.setText(chat.getOtherUser().getName());
                ViewHelper.updateUserPic(imgPic, chat.getOtherUser());
                if (favUserIds != null && favUserIds.contains(chat.getOtherUser().getUid())) {
                    cbFav.setChecked(true);
                }
            }
            cbFav.setOnClickListener(this);
            tvLastMsg.setText(chat.getLastMessage());
            tvUpdateTime.setText(sdf.format(chat.getLastUpdate()));
        }


        /**
         * catch user click event, and update observer via {@link #listener}
         * @param v
         * @return whether long click action is valid or not
         */
        @Override
        public void onClick(View v) {
            int action = ACTION_INVALID;
            if (v == super.itemView) {
                action = ACTION_CLICK_ON_ITEM_BODY;
            } else if (v == cbFav) {
                action = ACTION_CLICK_ON_FAV_ITEM;
            } else if (v == imgPic) {
                action = ACTION_CLICK_ON_ITEM_PIC;
            }

            if (action != ACTION_INVALID) {
                if (listener != null) {
                    listener.onViewInteracted(v, this, action);
                }
            }
        }
    }


}
