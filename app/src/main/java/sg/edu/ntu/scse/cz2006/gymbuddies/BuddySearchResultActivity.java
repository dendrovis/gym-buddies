package sg.edu.ntu.scse.cz2006.gymbuddies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;

import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.BuddyResultAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavBuddyRecord;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.listener.OnRecyclerViewInteractedListener;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DialogHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;


/**
 * This is the display buddy search result
 * it gets searching condition from Search buddies fragment, and perform query to Firestore
 * After getting response, it display other users on Recycler view. User can then interact and add them as buddy
 *
 * @author Chia Yu
 * @since 2019-09-28
 */
public class BuddySearchResultActivity extends AppCompatActivity implements AppConstants, OnRecyclerViewInteractedListener<BuddyResultAdapter.ViewHolder> {
    /**
     * TAG is unique identifier to for logging purpose
     */
    private final String TAG = "GB.act.bdSearchResult";
    /**
     * view reference to empty view, when there is no user matched the condition, this view will be displayed
     */
    private TextView tvEmptyMessage;
    /**
     * view reference to recycler view that display list of user that matching search conditions
     */
    private RecyclerView rvResult;
    /**
     * list of users that matched searching condition
     */
    private ArrayList<User> listUsers;
    /**
     * list of favoured users' uid, used to identify buddies
     */
    private ArrayList<String> listFavUserIds;
    /**
     * adapter to bind {@link #listUsers} onto {@link #rvResult}
     */
    private BuddyResultAdapter adapter;
    /**
     * Firestore instance to query data
     */
    private FirebaseFirestore firestore;
    /**
     * document reference used to query user's favoured buddies record,
     * response of query will be converted to {@link #favRecord}
     */
    private DocumentReference favBuddiesRef;
    /**
     * actual object that keeps records of user's favoured buddies record
     */
    private FavBuddyRecord favRecord;
    /**
     * observer to listen to changes on user's favoured record changes
     */
    private ListenerRegistration favRecordChangeListener;

    /**
     * query to search user based on search condition
     * @see #buildSearchQuery()
     */
    private Query querySearchUser;

    /**
     * Android framework lifecycle
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_search_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (getIntent().getExtras() == null) {
            DialogHelper.showDialogInvalidArgs(this);
        } else {
            Log.d(TAG, "has extras");
            Bundle data = getIntent().getExtras();
            for (String key : data.keySet()) {
                Log.d(TAG, key + ": " + data.get(key));
            }
        }

        // get reference to views
        tvEmptyMessage = findViewById(R.id.tv_empty_msg);
        rvResult = findViewById(R.id.rv_buddies);

        // set up views
        listUsers = new ArrayList<>();
        listFavUserIds = new ArrayList<>();
        adapter = new BuddyResultAdapter(listUsers, listFavUserIds);
        adapter.setOnRecyclerViewClickedListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);

        rvResult.setAdapter(adapter);
        rvResult.setLayoutManager(mLayoutManager);
        notifyAndUpdateView();


        firestore = FirebaseFirestore.getInstance();
        queryFavUserRecord();
        queryBuddy();
    }

    /**
     * Android framework lifecycle
     */
    @Override
    protected void onResume() {
        super.onResume();
        listenFavRecordChanges();
    }

    /**
     * Android framework lifecycle
     */
    @Override
    protected void onPause() {
        stopListenFavRecordChanges();
        super.onPause();
    }

    /**
     * Register as an observer to listen changes of searched user result
     */
    private void listenFavRecordChanges() {
        favRecordChangeListener = favBuddiesRef.addSnapshotListener((doc, e) -> {
            Log.d(TAG, "favBuddiesRef.addSnapshotListener -> onEvent ");
            if (e != null) {
                Log.w(TAG, "Listen failed", e);
                return;
            }
            readFavRecordDoc(doc);
        });
    }

    /**
     * Unregister itself to stop listening changes of searched user result
     */
    private void stopListenFavRecordChanges() {
        if (favRecordChangeListener != null) {
            favRecordChangeListener.remove();
            favRecordChangeListener = null;
        }
    }

    /**
     * perform query to retrieve current user's fav
     */
    private void queryFavUserRecord() {
        Log.d(TAG, "queryFavRecord");
        if (favBuddiesRef == null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            favBuddiesRef = firestore.collection(COLLECTION_FAV_BUDDY).document(uid);
        }

        favBuddiesRef.get().addOnSuccessListener((documentSnapshot) -> {
            Log.d(TAG, "favBuddiesRef.get() -> onSuccess " + documentSnapshot);
            readFavRecordDoc(documentSnapshot);
        }).addOnFailureListener((e) -> {
            Log.d(TAG, "favBuddiesRef.get() -> onFailed " + e.getMessage());
        });
    }


    /**
     * After query to firestore by {@link #queryFavUserRecord()},
     * this method reads response and update data to be display
     *
     * @param documentSnapshot
     */
    private void readFavRecordDoc(DocumentSnapshot documentSnapshot) {
        favRecord = documentSnapshot.toObject(FavBuddyRecord.class);
        if (favRecord == null) {
            favRecord = new FavBuddyRecord();
        }
        listFavUserIds.clear();
        listFavUserIds.addAll(favRecord.getBuddiesId());
    }

    /**
     * Craft search user query to firestore, query is based on search parameters are:
     * <ul>
     *     <li>preferred workout days</li>
     *     <li>preferred location</li>
     *     <li>preferred time</li>
     *     <li>preferred gender</li>
     * </ul>
     * @see sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdsearch.BuddySearchFragment
     */
    private Query buildSearchQuery(){
        int[] arrPrefDays = getIntent().getExtras().getIntArray("pref_days");
        String location = getIntent().getExtras().getString("pref_location");
        String time = getIntent().getExtras().getString("pref_time");
        String gender = getIntent().getExtras().getString("gender");


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userRef = db.collection(GymHelper.GYM_USERS_COLLECTION);

        // step 1: limit to location
        Query q = userRef.whereEqualTo("prefLocation", location);

        // step 2: by am/pm
        q = q.whereEqualTo("prefTime", time);

        // step 3: by gender
        if (!gender.equalsIgnoreCase("Both")) {
            q = q.whereEqualTo("gender", gender);
        }

        // step 4: by days
        if (arrPrefDays[0] == 1) {
            q = q.whereEqualTo(FieldPath.of("prefDay", "monday"), true);
        }
        if (arrPrefDays[1] == 1) {
            q = q.whereEqualTo(FieldPath.of("prefDay", "tuesday"), true);
        }
        if (arrPrefDays[2] == 1) {
            q = q.whereEqualTo(FieldPath.of("prefDay", "wednesday"), true);
        }
        if (arrPrefDays[3] == 1) {
            q = q.whereEqualTo(FieldPath.of("prefDay", "thursday"), true);
        }
        if (arrPrefDays[4] == 1) {
            q = q.whereEqualTo(FieldPath.of("prefDay", "friday"), true);
        }
        if (arrPrefDays[5] == 1) {
            q = q.whereEqualTo(FieldPath.of("prefDay", "saturday"), true);
        }
        if (arrPrefDays[6] == 1) {
            q = q.whereEqualTo(FieldPath.of("prefDay", "sunday"), true);
        }
        return q;
    }

    /**
     * perform query to Firestore, and update data based on response
     * @see #buildSearchQuery()
     */
    private void queryBuddy() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("loading");
        pd.show();

        if (querySearchUser == null){
            querySearchUser = buildSearchQuery();
        }
        querySearchUser.get().addOnSuccessListener(queryDocumentSnapshots->{
            pd.dismiss();
            listUsers.clear();
            for (DocumentSnapshot docSnapshot : queryDocumentSnapshots) {
                if (!docSnapshot.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    listUsers.add(docSnapshot.toObject(User.class));
                }
            }
            notifyAndUpdateView();
        }).addOnFailureListener(e->{
            e.printStackTrace();
            pd.dismiss();

            // display error message and allow user to retry
            Snackbar snackbar = Snackbar.make(rvResult, "Error: "+e.getMessage(), Snackbar.LENGTH_LONG);
            snackbar.setAction("Retry", v ->  queryBuddy());
            snackbar.show();
        });
    }


    /**
     * Handle option menu action
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
     * Listen to user interaction events from BuddyResultAdapter, and perform necessary actions
     */
    @Override
    public void onViewInteracted(View view, BuddyResultAdapter.ViewHolder holder, int action) {
        Log.d(TAG, "onBuddyItemClicked::action: " + action + ", pos: " + holder.getAdapterPosition() + ", view: " + view.getClass().getSimpleName());

        // TODO: handle event
        User otherUser = listUsers.get(holder.getAdapterPosition());
        switch (action) {
            case BuddyResultAdapter.ACTION_CLICK_ON_ITEM_BODY:
                Snackbar.make(rvResult, "To Chat, pos(" + holder.getAdapterPosition() + ")", Snackbar.LENGTH_LONG).show();
                goChatActivity(otherUser);
                break;

            case BuddyResultAdapter.ACTION_CLICK_ON_ITEM_PIC:
                DialogHelper.displayBuddyProfile(this, otherUser, ((ImageView) view).getDrawable());
                break;

            case BuddyResultAdapter.ACTION_CLICK_ON_FAV_ITEM:
                Snackbar.make(rvResult, "To Fav buddy, pos(" + holder.getAdapterPosition() + ")", Snackbar.LENGTH_LONG).show();
                if (view instanceof CheckBox) {
                    CheckBox cbFav = (CheckBox) view;
                    if (cbFav.isChecked()) {
                        doAddFavBuddy(otherUser);
                    } else {
                        doRemoveFavBuddy(otherUser);
                    }
                }
                break;
            default:
                Snackbar.make(rvResult, "Action(" + action + ") undefined", Snackbar.LENGTH_LONG).show();
                break;
        }
    }


    /**
     * Add another user as buddies,
     * uid will be added to favourite list and update to firestore
     * @param otherUser
     * @see #commitFavRecord()
     */
    private void doAddFavBuddy(User otherUser) {
        Log.d(TAG, "doAddFavBuddy->" + otherUser.getUid());
        if (!listFavUserIds.contains(otherUser.getUid())) {
            listFavUserIds.add(otherUser.getUid());
            favRecord.getBuddiesId().add(otherUser.getUid());
            commitFavRecord();
        }
    }

    /**
     * Remove buddy,
     * Uid will be removed from favourite list and update to firestore
     * @param otherUser
     * @see #commitFavRecord()
     */
    private void doRemoveFavBuddy(User otherUser) {
        Log.d(TAG, "doRemoveFavBuddy->" + otherUser.getUid());
        if (listFavUserIds.contains(otherUser.getUid())) {
            listFavUserIds.remove(otherUser.getUid());
            favRecord.getBuddiesId().remove(otherUser.getUid());
            commitFavRecord();
        }
    }

    /**
     * query to FirebaseFirestore to update
     */
    private void commitFavRecord() {
        firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                transaction.set(favBuddiesRef, favRecord);
                return null;
            }
        }).addOnSuccessListener((v) -> {
            Log.d(TAG, "favRecord updated success");
        }).addOnFailureListener((e) -> {
            Log.d(TAG, "favRecord updated failed");
            e.printStackTrace();
        });
    }

    /**
     * notify recycler view about data is being modified.
     * if nothing to display, it display empty view
     */
    private void notifyAndUpdateView() {
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() == 0) {
            rvResult.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            rvResult.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    /**
     * Redirect user to chat activity
     * @param other
     */
    private void goChatActivity(User other) {
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle data = new Bundle();
        data.putString("buddy_id", other.getUid());
        data.putString("buddy_name", other.getName());
        data.putString("buddy_pic_url", other.getProfilePicUri());
        intent.putExtras(data);
        startActivity(intent);
    }
}
