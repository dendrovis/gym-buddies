package sg.edu.ntu.scse.cz2006.gymbuddies.ui.chatlist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.ntu.scse.cz2006.gymbuddies.AppConstants;
import sg.edu.ntu.scse.cz2006.gymbuddies.ChatActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.ChatAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Chat;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavBuddyRecord;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewInteractedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DialogHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;


/**
 *  The fragment display list of buddies (favoured user) of current user
 *  it allow user to view profile information, start chat and un-favour of user
 *
 * @author Chia Yu
 * @since 2019-09-06
 */
public class ChatListFragment extends Fragment implements AppConstants, OnRecyclerViewInteractedListener<ChatAdapter.ChatViewHolder> {
    /**
     * TAG is unique identifier to for logging purpose
     */
    private static final String TAG = "gb.frag.chatlist";
    /**
     * view model to separate data from activity life cycle
     */
    private ChatListViewModel chatListViewModel;

    /**
     * reference to SwipeRefreshLayout, allow user to drag down for refresh data
     */
    private SwipeRefreshLayout srlUpdateChats;
    /**
     * list to hold queried chat room data
     */
    private ArrayList<Chat> chats;
    /**
     * list to hold buddies' user id
     */
    private ArrayList<String> favUserIds;
    /**
     * view reference to empty view, when there is no user matched the condition, this view will be displayed
     */
    private TextView tvEmptyMessage;
    /**
     * reference to RecyclerView, used to display chat room data on screen
     */
    private RecyclerView rvChats;
    /**
     * handle binding of data to be display on RecyclerView
     */
    private ChatAdapter adapter;

    /**
     * Firestore instance to query data
     */
    private FirebaseFirestore firestore;

    /**
     * document reference used to query user's favoured buddies record
     */
    private DocumentReference favBuddiesRef;
    /**
     * Observer to listen to changes on user's favour buddies record
     */
    private ListenerRegistration queryFavListener;
    /**
     * Observer to listen to changes on chat room records
     */
    private ListenerRegistration queryChatListener;
    /**
     * query on chat room has current user involved in
     */
    private Query queryChats;


    /**
     * Android framework lifecycle
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatListViewModel = ViewModelProviders.of(this).get(ChatListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chat_list, container, false);
//        final TextView textView = root.findViewById(R.id.text_send);
        chatListViewModel.getText().observe(this, s -> { /*textView.setText(s);*/
        });


        tvEmptyMessage = root.findViewById(R.id.tv_empty_msg);
        rvChats = root.findViewById(R.id.rv_chats);
        srlUpdateChats = root.findViewById(R.id.srl_update_chats);
        srlUpdateChats.setColorSchemeResources(R.color.google_1, R.color.google_2, R.color.google_3, R.color.google_4);
        srlUpdateChats.setOnRefreshListener(() -> {
            // do something
            stopListenChatListChanges();
            listenChatListChanges();
        });


        chats = new ArrayList<>();
        favUserIds = new ArrayList<>();
        adapter = new ChatAdapter(chats, favUserIds);
        adapter.setOnRecyclerViewClickedListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager( mLayoutManager );


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
        CollectionReference chatRef = firestore.collection(AppConstants.COLLECTION_CHAT);
        queryChats = chatRef.whereEqualTo(FieldPath.of(  "participant", uid), true);

        return root;
    }

    /**
     * android framework lifecycle
     */
    @Override
    public void onResume() {
        super.onResume();
        listenChatListChanges();
        listenFavChanges();
    }

    /**
     * android framework lifecycle
     */
    @Override
    public void onPause() {
        stopListenChatListChanges();
        stopListenFavChanges();
        super.onPause();
    }

    /**
     * notify recycler view about data is being modified.
     * if nothing to display, it display empty view
     */
    private void notifyAndUpdateView(){
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() == 0) {
            rvChats.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            rvChats.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    /**
     * register an observer to listen to changes on user's favoured buddies records
     */
    private void listenFavChanges(){
        if (favBuddiesRef == null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            favBuddiesRef = firestore.collection(COLLECTION_FAV_BUDDY).document(uid);
        }
        queryFavListener = favBuddiesRef.addSnapshotListener((doc, e)->{
            if (e!=null){
                e.printStackTrace();
                return;
            }

            favUserIds.clear();
            if (doc != null){
                FavBuddyRecord fav = doc.toObject(FavBuddyRecord.class);
                if (fav!=null){
                    favUserIds.addAll(fav.getBuddiesId());
                }
            }
            notifyAndUpdateView();
        });
    }

    /**
     * unregister observer to listen to changes on  user's favoured buddies records
     */
    private void stopListenFavChanges(){
        if (queryFavListener!=null){
            queryFavListener.remove();
            queryFavListener = null;
        }

    }


    /**
     *  register an observer to chat room records changes
     */
    private void listenChatListChanges(){
        queryChatListener = queryChats.addSnapshotListener((queryDocumentSnapshots, e)->{
            Log.d(TAG, "chat query -> onEvent ("+queryDocumentSnapshots+", "+e+")");
            if (e != null){
                Log.d(TAG, "error: "+e.getMessage());
                e.printStackTrace();
                return;
            }

            // read doc snapshots
            if (queryDocumentSnapshots==null){
                Log.d(TAG, "chat query -> onEvent.snapshots are null");
                return;
            }
            readQuerySnapshot(queryDocumentSnapshots);
        });
    }

    /**
     *  unregister an observer to chat room records changes
     */
    private void stopListenChatListChanges(){
        if (queryChatListener!=null){
            queryChatListener.remove();
            queryChatListener = null;
        }
    }

    /**
     * After {@link #listenChatListChanges()} retrieve changes of chat room,
     * reads changes from snapshots and update data respectively
     * @param snapshots
     */
    private void readQuerySnapshot(QuerySnapshot snapshots){
        Log.d(TAG, "reading chat data "+snapshots);
        ArrayList<Chat> oldChats = (ArrayList<Chat>) chats.clone();
        chats.clear();
        Chat temp;
        for (DocumentSnapshot doc:snapshots){
            temp = doc.toObject(Chat.class);
            temp.setChatId(doc.getId());
            if (temp.getLastUpdate()!=0){
                chats.add( temp );
            }
        }

        // sort chats by last update time
        Collections.sort(chats, (c1,c2)-> (int)(c2.getLastUpdate()-c1.getLastUpdate()));
        Log.d(TAG, "chat size -> "+chats.size());

        // mapping with old chat and decide whether need to query user data
        if (oldChats.size() == 0){
            queryUser();
        } else {
            boolean needQueryUser = false;
            for (Chat chat : chats){
                for(int i=oldChats.size()-1; i>=0; i--){
                    Chat oldChat = oldChats.get(i);
                    if (chat.getChatId().equals(oldChat.getChatId())){
                        chat.setOtherUser(oldChat.getOtherUser());
                        oldChats.remove(oldChat);
                    }
                }
                if (chat.getOtherUser() == null){
                    needQueryUser = true;
                }
            }
            notifyAndUpdateView();
            if (needQueryUser){
                queryUser();
            }
        }
        if (srlUpdateChats.isRefreshing()){
            srlUpdateChats.setRefreshing(false);
        }
    }

    /**
     * After chat room data is retrieved from firestore,
     * make another query to retrieve involved member profile for chat rooms
     */
    private void queryUser(){
        Log.d(TAG, "query for user");
        CollectionReference userRef = firestore.collection(GymHelper.GYM_USERS_COLLECTION);
        userRef.get().addOnSuccessListener((snapshots)->{
            Log.d(TAG, "userRef.get() success");
            readUserAndUpdateChats(snapshots);
        }).addOnFailureListener((e)-> Log.d(TAG, "query all users failed"));
    }

    /**
     * After {@link #queryUser()} retrieve list of user profile,
     * reads changes from snapshots and update chat rooms with user involved
     * @param snapshots
     */
    private void readUserAndUpdateChats(QuerySnapshot snapshots){
        Log.d(TAG, "readUserAndUpdateChats -> "+snapshots);
        ArrayList<User> users = new ArrayList<>();
        users.addAll(snapshots.toObjects(User.class));
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        for (Chat chat:chats){
            String otherUid = "";
            for (String userId:chat.getParticipant().keySet()) {
                if (!uid.equals(userId)){
                    otherUid = userId;
                    break;
                }
            }
            if (otherUid.length()==0){ continue; }
            for (User user:users){
                if (otherUid.equals(user.getUid())){
                    chat.setOtherUser(user);
                    break;
                }
            }
        }
        notifyAndUpdateView();
    }


    /**
     * redirect user to chat activity
     * @param chat
     */
    private void goChatActivity(Chat chat){
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        Bundle data = new Bundle();
        data.putString("chat_id", chat.getChatId());
        if (chat.getOtherUser() != null){
            data.putString("buddy_name", chat.getOtherUser().getName());
            data.putString("buddy_pic_url", chat.getOtherUser().getProfilePicUri());
        }

        intent.putExtras(data);
        startActivity(intent);
    }

    /**
     * interface for observer to handle event when subject notify the view interaction of RecyclerView
     * @param view
     * @param holder
     * @param action
     */
    @Override
    public void onViewInteracted(View view, ChatAdapter.ChatViewHolder holder, int action) {
        int pos = holder.getAdapterPosition();
        if (pos<0 || pos>=chats.size()){
            Log.d(TAG, "invalid pos: "+pos);
            return;
        }

        Chat chat = chats.get(pos);
        switch (action){
            case ChatAdapter.ACTION_CLICK_ON_ITEM_PIC:
                if (chat.getOtherUser()!=null){
                    DialogHelper.displayBuddyProfile(getContext(), chat.getOtherUser(), ((ImageView)view).getDrawable() );
                }
                break;
            case ChatAdapter.ACTION_CLICK_ON_ITEM_BODY:
                goChatActivity(chat);
                break;
            case ChatAdapter.ACTION_CLICK_ON_FAV_ITEM:
                String bdUid = chat.getOtherUser().getUid();
                CheckBox cbFav = (CheckBox) view;
                if (cbFav.isChecked()){
                    favUserIds.add(bdUid);
                } else {
                    favUserIds.remove(bdUid);
                }
                commitFavRecord();
                break;
            default:
                Log.d(TAG, "unknown action -> "+action);
                Snackbar.make(getView(), "action implementing", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * commit changes of removing user from buddies record in firestore
     */
    private void commitFavRecord() {
        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            FavBuddyRecord favRecord = transaction.get(favBuddiesRef).toObject(FavBuddyRecord.class);
            favRecord.getBuddiesId().clear();
            favRecord.getBuddiesId().addAll(favUserIds);

            transaction.set(favBuddiesRef, favRecord);
            return null;
        }).addOnSuccessListener((v) -> {
            Log.d(TAG, "favRecord updated success");
        }).addOnFailureListener((e) -> {
            Log.d(TAG, "favRecord updated failed");
            e.printStackTrace();
        });
    }
}