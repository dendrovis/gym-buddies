package sg.edu.ntu.scse.cz2006.gymbuddies;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.MessageAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Chat;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.ChatMessage;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewInteractedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DialogHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DiskIOHelper;

/**
 * The Activity displaying chat history between another user, and allows user to sent and receive message.
 *
 * @author Chia Yu
 * @since 2019-10-22
 */
public class ChatActivity extends AppCompatActivity implements AppConstants, View.OnClickListener, OnRecyclerViewInteractedListener {
    /**
     * TAG is unique identifier to for logging purpose
     */
    private static final String TAG = "gb.act.chat";
    /**
     * View reference to EditText, which allow user to input message
     */
    private EditText etMessage;
    /**
     * View reference to ImageButton, allow user to send out message
     */
    private ImageButton imgBtnSend;
    /**
     * view reference to TextView, that shows title of chat room
     */
    private TextView tvTitle;
    /**
     * view reference to ImageView, that shows icon of chat room
     */
    private ImageView imgBuddyPic;
    /**
     * list of chat messages within chat room
     */
    private ArrayList<ChatMessage> messages;
    /**
     * view reference to RecyclerView, which display {@link #messages} to user with help of {@link #adapter}
     */
    RecyclerView rvMessages;
    /**
     * adapter to bind {@link #messages} onto {@link #rvMessages}
     */
    MessageAdapter adapter;
    /**
     * Firestore instance to query data
     */
    FirebaseFirestore firestore;
    /**
     * document reference used to get current chat room's info,
     * response of query will be converted to {@link #curChat}
     */
    DocumentReference chatRef;
    /**
     * observer to listen to changes on messages changes in current chat room
     */
    ListenerRegistration msgListener;
    /**
     * actual chat object holds information of current chat room
     */
    Chat curChat;
    /**
     * current user's id
     */
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    /**
     * Android framework life cycle
     * create reference to frequently update view, and set up adapter to display messages
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // init views
        etMessage = findViewById(R.id.et_msg);
        tvTitle = findViewById(R.id.toolbar_title);
        imgBuddyPic = findViewById(R.id.img_bd_pic);
        imgBtnSend = findViewById(R.id.btn_send);
        rvMessages = findViewById(R.id.rv_messages);

        imgBtnSend.setOnClickListener(this);
        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, uid);
        adapter.setOnRecyclerViewInteractListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvMessages.setLayoutManager(mLayoutManager);
        rvMessages.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



        firestore = FirebaseFirestore.getInstance();

        // check for pass over data
        init();
    }

    /**
     * the method handle on data received from other activity, and based on the data to initialise activity.
     */
    private void init(){
        // testing pass over data
        for(String key : getIntent().getExtras().keySet()){
            Log.d(TAG, String.format("%s -> %s", key, getIntent().getStringExtra(key)));
        }


        boolean needUserPic = true;
        if (getIntent().hasExtra("chat_id")){
            // load chat > load message
            queryChatByChatId();
        } else if (getIntent().hasExtra("buddy_id")){
            String buddyId = getIntent().getStringExtra("buddy_id");
            //imgBuddyPic
            if (DiskIOHelper.hasImageCache(this, buddyId)){
                RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(getResources(), DiskIOHelper.readImageCache(this, buddyId));
                roundBitmap.setCircular(true);
                imgBuddyPic.setImageDrawable(roundBitmap);
                imgBuddyPic.setTag(buddyId);
                needUserPic = false;
            }
            queryChatByParticipants();
            // find or create chat > load message;
        } else {
            DialogHelper.showDialogInvalidArgs(this);
        }

        if (getIntent().hasExtra("buddy_name")){
            tvTitle.setText(getIntent().getStringExtra("buddy_name"));
        }

        if (needUserPic && getIntent().hasExtra("buddy_pic_url")){
            getProfilePic(getIntent().getStringExtra("buddy_pic_url"));
        }
    }


    /**
     * Android framework lifecycle, register an observer to listen to chat message changes
     */
    @Override
    protected void onResume() {
        super.onResume();
        listenToMessages();
    }

    /**
     * Android framework lifecycle, unregister an observer to listen to chat message changes
     */
    @Override
    protected void onPause() {
        stopListenToMessages();
        super.onPause();
    }


    /**
     * The method handle query about chat details from FirebaseFirestore if chat id is provided
     */
    private void queryChatByChatId(){
        Log.d(TAG, "query Chat by chat Id");
        String chatId = getIntent().getStringExtra("chat_id");
        chatRef = firestore.collection(AppConstants.COLLECTION_CHAT).document(chatId);
        chatRef.get().addOnSuccessListener((documentSnapshot)->{
            curChat = documentSnapshot.toObject(Chat.class);

            listenToMessages();
        }).addOnFailureListener((e)->{
            Log.d(TAG, "query chat failed, e-> "+e.getMessage());
            e.printStackTrace();
            Snackbar.make(rvMessages, "Error(get chat by cid): "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
        });
    }

    /**
     * The method handle query about chat details from FirebaseFirestore if chat id is not provided,
     * attempt to find chat detail based on member participation
     */
    public void queryChatByParticipants(){
        Log.d(TAG, "find existing chat by uid(self and other)");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUid = getIntent().getStringExtra("buddy_id");


        CollectionReference chatCollectionRef = firestore.collection(AppConstants.COLLECTION_CHAT);
        Query queryChats = chatCollectionRef.whereEqualTo(FieldPath.of(  "participant", uid), true)
                .whereEqualTo(FieldPath.of(  "participant", otherUid), true);



        queryChats.get().addOnSuccessListener((snapshots)->{
            if (snapshots == null){
                Log.d(TAG, "queryChatByParticipants.OnSuccess: snapshots is null");
                Snackbar.make(rvMessages, "Error(get chat by uids): something is wrong", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "chat.size"+snapshots.size());
            for (DocumentSnapshot snapshot:snapshots) {
                Log.d(TAG, "doc id->"+snapshot.getId()+", "+snapshot.toObject(Chat.class));
            }

            // read chat or create new chat
            if (snapshots.size() == 0){
                queryCreateChat();
            } else {
                curChat = snapshots.toObjects(Chat.class).get(0);
                curChat.setChatId( snapshots.getDocuments().get(0).getId());
                chatRef = firestore.collection(COLLECTION_CHAT).document(curChat.getChatId());
                // load messages
                listenToMessages();
            }
        }).addOnFailureListener((e)->{
            Log.d(TAG, "Error query chat by participant");
            e.printStackTrace();
            Snackbar.make(rvMessages, "Error(get chat by uids): "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
        });
    }

    /**
     * The method create new chat record, once there is no chat found in FirebaseFirestore
     */
    private void queryCreateChat(){
        Log.d(TAG, "Create new chat");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String otherUid = getIntent().getStringExtra("buddy_id");
        curChat = new Chat();
        curChat.getParticipant().put(uid, true);
        curChat.getParticipant().put(otherUid, true);

        firestore.collection(AppConstants.COLLECTION_CHAT).add(curChat)
                .addOnSuccessListener((DocumentReference documentReference)->{
                    Log.d(TAG, "queryCreateChat -> success("+documentReference.getId()+"");
                    curChat.setChatId(documentReference.getId());
                    chatRef = firestore.collection(AppConstants.COLLECTION_CHAT).document(documentReference.getId());
                    listenToMessages();

                }).addOnFailureListener((e)->{
                    Log.d(TAG, "queryCreateChat -> failed("+e.getMessage()+"");
                    e.printStackTrace();
                    Snackbar.make(rvMessages, "Error(new chat): "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
        });

    }

    /**
     * Register an observer on chat messages changes
     */
    private void listenToMessages(){
        Log.d(TAG, "listen to message");
        if (chatRef == null || (curChat !=null && curChat.getChatId()==null)){
            Log.d(TAG, "Missing Chat ID, Cannot listen to messages");
            return;
        };

        msgListener = chatRef.collection(COLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
            Log.d(TAG, "Listen to message changes");
            if (e != null){
                Log.d(TAG, "Listen failed -> "+e.getMessage());
                e.printStackTrace();
                return;
            }

            messages.clear();
            if (snapshots != null){
                messages.addAll( snapshots.toObjects(ChatMessage.class));
            }else {
                Log.d(TAG, "snapshots are null");
            }

            // sort messages by last update time
            Collections.sort(messages, (m1, m2)-> (int)(m1.getTimestamp()-m2.getTimestamp()));

            adapter.notifyDataSetChanged();
            rvMessages.scrollToPosition(messages.size()-1);
        });
    }

    /**
     * Unregister observer on chat message changes
     */
    private void stopListenToMessages(){
        if (msgListener != null){
            msgListener.remove();
            msgListener = null;
        }
    }

    /**
     * Handle option menu action like back button pressed on title bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * The method provides implementation for View.OnClickListener
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v == imgBtnSend){
            String message = etMessage.getText().toString();
            if (message.length()>0){
                Log.d(TAG, "msg -> "+message);

                // do something
                ChatMessage newMsg = new ChatMessage(message, uid);
                commitMessage(newMsg);
                messages.add(newMsg);
                adapter.notifyDataSetChanged();
                etMessage.setText("");
                rvMessages.scrollToPosition(messages.size()-1);
            }

        }

    }

    /**
     * The method query another user's profile picture as chat icon
     * @param imgUrl
     */
    private void getProfilePic(String imgUrl){
        if (imgUrl.equals(imgBuddyPic.getTag())){
            return;
        }

        new GetProfilePicFromFirebaseAuth(this, new GetProfilePicFromFirebaseAuth.Callback() {
            @Override
            public void onComplete(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    roundBitmap.setCircular(true);

                    imgBuddyPic.setImageDrawable(roundBitmap);
                    imgBuddyPic.setTag(imgUrl);
                }
            }
        }).execute(Uri.parse(imgUrl));

    }



    /**
     * The method performs create/update of chat message on FirebaseFirestore
     * @param msg
     */
    private void commitMessage(ChatMessage msg){
        if (chatRef == null){
            Log.d(TAG, "chatRef is null");
            return;
        }

        final DocumentReference messageRef = chatRef.collection(COLLECTION_MESSAGES).document();
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                curChat.setLastMessage(msg.getMessage());
                curChat.setLastUpdate(msg.getTimestamp());

                transaction.set(chatRef, curChat);
                transaction.set(messageRef, msg);
                return null;
            }
        }).addOnSuccessListener((v)->{
            Log.d(TAG, "Message updated success");
        }).addOnFailureListener((e)->{
            Log.d(TAG, "Message updated failed");
            e.printStackTrace();
        });
    }

    /**
     * The method performs recall of chat message on FirebaseFirestore
     * @param msg
     */
    private void recallMessage(ChatMessage msg){
        if (chatRef == null){
            Log.d(TAG, "chatRef is null");
            return;
        }

        final DocumentReference messageRef = chatRef.collection(COLLECTION_MESSAGES).document();
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {


                if (msg.getTimestamp() == curChat.getLastUpdate()){
                    curChat.setLastMessage("The message is deleted");
                    transaction.set(chatRef, curChat);
                }

                transaction.set(messageRef, msg);
                return null;
            }
        }).addOnSuccessListener((v)->{
            Log.d(TAG, "Message recalled success");
        }).addOnFailureListener((e)->{
            Log.d(TAG, "Message recalled failed");
            e.printStackTrace();
        });
    }


    /**
     * The method provides the implementation on user interacting with RecyclerView's items
     * @param view
     * @param holder
     * @param action
     * @see MessageAdapter
     * @see OnRecyclerViewInteractedListener
     *
     */
    @Override
    public void onViewInteracted(View view, RecyclerView.ViewHolder holder, int action) {
        Log.d(TAG, "onViewInteracted:: action -> "+action);
        switch (action){
            case MessageAdapter.ACTION_LONG_CLICK_ON_VIEW:
                int pos = holder.getAdapterPosition();
                if (pos>=0){
                    final ChatMessage msg = messages.get(pos);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Recall message")
                            .setMessage("The action cannot be undone")
                            .setPositiveButton("Delete", (dialogInterface, i)->{
                                msg.setMessage(null);
                                recallMessage(msg);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();

                }
                break;
            default:
                Log.d(TAG, "onViewInteracted:: Unknown action -> "+action);
                break;
        }
    }
}
