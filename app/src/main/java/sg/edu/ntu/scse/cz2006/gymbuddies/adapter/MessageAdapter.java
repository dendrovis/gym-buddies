package sg.edu.ntu.scse.cz2006.gymbuddies.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.ChatMessage;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewInteractedListener;

/**
 * Recycler Adapter for Chat Message
 * For sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 *
 * The adapter is used to display chat message between users.
 *
 * @author Chia Yu
 * @since 2019-10-19
 */
public class MessageAdapter extends RecyclerView.Adapter {
    /**
     * TAG is unique identifier to for logging purpose
     */
    private static final String TAG = "gb.adapter.msg";
    /**
     * unique identifier to specify user long clicked on a view
     * @see #listener
     */
    public static final int ACTION_LONG_CLICK_ON_VIEW = 1;
    /**
     * unique identifier to type of view to be render for {@link ChatMessage} object
     * @see #getItemViewType(int)
     */
    private static final int VIEW_TYPE_MSG_SENT = 1;
    /**
     * unique identifier to type of view to be render for {@link ChatMessage} object
     * @see #getItemViewType(int)
     */
    private static final int VIEW_TYPE_MSG_RECEIVED = 2;
    /**
     * It is used to format timestamp from long to String for {@link ChatMessage} time
     */
    private SimpleDateFormat sdfTime;
    /**
     * It is used to format timestamp from long to String for {@link ChatMessage} date
     */
    private SimpleDateFormat sdfDate;
    /**
     * UID of current user, used to identify message is sent out or received
     * @see sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User
     * @see #getItemViewType(int)
     */
    private String sender;
    /**
     * list that holds the chat history with another user in the same chat room
     */
    private ArrayList<ChatMessage> messages;
    /**
     * interface that allow activity to observe user interaction with RecyclerView
     */
    private OnRecyclerViewInteractedListener listener;

    /**
     * Constructor method to set up MessageAdapter
     * @param messages list of messages
     * @param sender uid of current user
     */
    public MessageAdapter( ArrayList<ChatMessage> messages, String sender){
        this.messages = messages;
        this.sender = sender;
        this.sdfTime = new SimpleDateFormat("HH:mm");
        this.sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    }

    /**
     * The method provides an interface to allow other class to register itself as observer of user interacting event
     *
     * @param listener
     * @see OnRecyclerViewInteractedListener
     */
    public void setOnRecyclerViewInteractListener(OnRecyclerViewInteractedListener listener){
        this.listener = listener;
    }

    /**
     * the method is return number of messages to be display on RecyclerView
     */
    @Override
    public int getItemCount() {
        return this.messages.size();
    }


    /**
     * the method return type of view based on current user id
     * view type will determine either message sent or received item layout to be rendered on screen
     * @param position position of displaying view
     * @return type of view
     */
    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getSender().equals(sender)){
            return VIEW_TYPE_MSG_SENT;
        }else {
            return VIEW_TYPE_MSG_RECEIVED;
        }
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MSG_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_msg_send, parent, false);
            return new MessageSentViewHolder(view);
        } else if (viewType == VIEW_TYPE_MSG_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_msg_receive, parent, false);
            return new MessageReceivedViewHolder(view);
        }
        return null;
    }

    /**
     * this method make use of view holder to update display information onto the view
     *
     * @param holder view holder of updating view
     * @param position position of current view
     * @see #getItemViewType(int)
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        boolean showDate = false;
        if (position == 0){
            showDate = true;
        } else if (!sdfDate.format(message.getTimestamp()).equals(sdfDate.format(messages.get(position-1).getTimestamp()))){
            showDate = true;
        }
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MSG_SENT:
                ((MessageSentViewHolder) holder).bind(message, showDate);
                break;
            case VIEW_TYPE_MSG_RECEIVED:
                ((MessageReceivedViewHolder) holder).bind(message, showDate);
        }
    }


    /**
     * View Holder for message received from other user,
     * it holds reference to frequent update views and provides helper method to bind data information onto views
     */
    public class MessageSentViewHolder extends RecyclerView.ViewHolder  implements View.OnLongClickListener {
        TextView tvContent, tvTime, tvDate;

        /**
         * Constructor method to initialised MessageSentViewHolder
         * @param itemView
         */
        public MessageSentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_msg_body);
            tvTime = itemView.findViewById(R.id.tv_msg_time);
            tvDate = itemView.findViewById(R.id.tv_msg_date);
            tvContent.setOnLongClickListener(this);
        }

        /**
         * helper method to update respective view based on data pass over
         * @param msg
         * @param showDate
         */
        public void bind(ChatMessage msg, boolean showDate){
            tvContent.setText( msg.getMessage()==null?"The message is deleted": msg.getMessage() );
            tvTime.setText( sdfTime.format(msg.getTimestamp()) );
            tvDate.setText( sdfDate.format(msg.getTimestamp()));
            tvDate.setVisibility(showDate?View.VISIBLE:View.GONE);
        }

        /**
         * catch user long click event, and update observer via {@link #listener}
         * @param view
         * @return whether long click action is valid or not
         */
        @Override
        public boolean onLongClick(View view) {
            Log.d(TAG, "on long clicked, listener -> "+listener);
            if (view == tvContent){
                if (listener != null){
                    listener.onViewInteracted(view, this, ACTION_LONG_CLICK_ON_VIEW);
                    Log.d(TAG, "notify");
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * View Holder for message sent out from current user,
     * it holds reference to frequent update views and provides helper method to bind data information onto views
     */
    public class MessageReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime, tvDate;

        /**
         * Constructor method to initialised MessageReceivedViewHolder
         * @param itemView
         */
        public MessageReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_msg_body);
            tvTime = itemView.findViewById(R.id.tv_msg_time);
            tvDate = itemView.findViewById(R.id.tv_msg_date);
        }

        /**
         * helper method to update respective view based on data pass over
         * @param msg
         * @param showDate
         */
        public void bind(ChatMessage msg, boolean showDate){
            tvContent.setText( msg.getMessage()==null?"The message is deleted": msg.getMessage() );
            tvTime.setText( sdfTime.format(msg.getTimestamp()) );
            tvDate.setText( sdfDate.format(msg.getTimestamp()));
            tvDate.setVisibility(showDate?View.VISIBLE:View.GONE);
        }
    }
}
