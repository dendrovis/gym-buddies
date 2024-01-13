package sg.edu.ntu.scse.cz2006.gymbuddies.ui.gymlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import sg.edu.ntu.scse.cz2006.gymbuddies.CarparkAndSearchResultActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.FavGymAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.GymReviewAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.StringRecyclerAdapter;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FirestoreRating;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymRatingStats;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymRatings;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateGymFavourites;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.ProfilePicHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.SwipeDeleteCallback;
import sg.edu.ntu.scse.cz2006.gymbuddies.widget.FavButtonView;

/**
 * The fragment handling the viewing of just the favourited gyms of the user
 *
 * @author Kenneth Soh
 * @since 2019-10-03
 */
public class GymListFragment extends Fragment implements SwipeDeleteCallback.ISwipeCallback {

    /**
     * The Fragment View Model as per MVVM architecture
     */
    private GymListViewModel gymListViewModel;

    /**
     * The coordinates of the selected gym
     */
    private LatLng coordinates = null;

    /**
     * Debug tag for logging purposes
     */
    private static final String TAG = "GymListFrag";

    /**
     * The coordinator layout handling the fragment
     */
    private CoordinatorLayout coordinatorLayout;

    /**
     * An adapter to store the favourites list for the RecyclerView
     */
    private FavGymAdapter favAdapter = null;

    /**
     * Firebase Firestore Favourites List Real-time listener
     */
    private ListenerRegistration favListener;

    /**
     * The favourites list recyclerview
     */
    private RecyclerView favouritesList;

    /**
     * The Firebase Firestore listener for favourites in the gym details bottom sheet
     * This is used to handle real time updates to the favourites count of the gym
     */
    private ListenerRegistration gymDetailFavListener = null;

    /**
     * Flag to determine if we are initializing the details view when the gym details bottom sheet appears
     */
    private boolean detailsInit = false;

    /**
     * If the user is currently reviewing a gym
     */
    private boolean flagReviewing = false;

    /**
     * The gym details bottom sheet
     */
    private View gymBottomSheet;

    /**
     * Gym Reviews recyclerview
     */
    private RecyclerView reviews;

    /**
     * Swipe Refresh Listener
     */
    private SwipeRefreshLayout swipeToRefresh;

    /**
     * Internal lifecycle fragment for creating the fragment view
     * @param inflater Layout Inflater object
     * @param container View Group Container object
     * @param savedInstanceState Android Saved Instance State
     * @return The created fragment view
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        gymListViewModel = ViewModelProviders.of(this).get(GymListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gym_list, container, false);
        coordinatorLayout = root.findViewById(R.id.coordinator);

        // Setup main view
        favouritesList = root.findViewById(R.id.recycler_view);
        if (favouritesList != null) {
            favouritesList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            favouritesList.setLayoutManager(llm);
            favouritesList.setItemAnimator(new DefaultItemAnimator());

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeDeleteCallback(this, root.getContext(), ItemTouchHelper.LEFT, R.drawable.ic_heart_off));
            itemTouchHelper.attachToRecyclerView(favouritesList);
        }
        emptyFavourites();

        swipeToRefresh = root.findViewById(R.id.swipe_refresh);
        swipeToRefresh.setOnRefreshListener(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).whereArrayContains("userIds", user.getUid()).get().addOnSuccessListener(GymListFragment.this::processFavListUpdates);
            }
        });
        swipeToRefresh.setColorSchemeResources(R.color.google_1, R.color.google_2, R.color.google_3, R.color.google_4);

        // Setup Gym Details View (sync with the gym section of the Home frag)
        TextView gymTitle = root.findViewById(R.id.gym_details_title);
        TextView gymLocation = root.findViewById(R.id.gym_details_location);
        TextView gymDesc = root.findViewById(R.id.gym_details_description);
        TextView favCount = root.findViewById(R.id.gym_details_fav_count);
        FavButtonView heartIcon = root.findViewById(R.id.gym_details_fav_icon);
        LinearLayout carpark = root.findViewById(R.id.gym_details_nearby_carparks_btn);
        reviews = root.findViewById(R.id.review_recycler);

        if (reviews != null) {
            reviews.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            reviews.setLayoutManager(llm);
            reviews.setItemAnimator(new DefaultItemAnimator());
        }

        gymBottomSheet = root.findViewById(R.id.gym_details_sheet);
        LinearLayout favourite = gymBottomSheet.findViewById(R.id.gym_details_fav);
        BottomSheetBehavior gymBottomSheetBehavior = BottomSheetBehavior.from(gymBottomSheet);
        gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        gymBottomSheet.findViewById(R.id.drag_bar).setVisibility(View.INVISIBLE);
        BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d("GymDetailsSheet", "State Changed: " + newState);
                if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED)
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("View Gym Detail");
                    else
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.menu_gym_list);
                }
                gymTitle.setSingleLine(newState == BottomSheetBehavior.STATE_COLLAPSED);
                if (detailsInit && newState != BottomSheetBehavior.STATE_SETTLING) {
                    detailsInit = false;
                    gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Not used
            }
        };
        gymBottomSheetBehavior.setBottomSheetCallback(callback);

        // Rating Stuff
        MaterialRatingBar editRatingBar = gymBottomSheet.findViewById(R.id.gym_details_rate_write);
        ImageView profilePic = gymBottomSheet.findViewById(R.id.profile_pic);
        if (ProfilePicHelper.getProfilePic() != null) profilePic.setImageDrawable(ProfilePicHelper.getProfilePic());
        else ProfilePicHelper.getProfilePicUpdateListener().add(profilePic::setImageDrawable);

        editRatingBar.setOnRatingChangeListener((ratingBar, rating) -> {
            if (flagReviewing) { flagReviewing = false; return; }
            View review = getLayoutInflater().inflate(R.layout.dialog_review, null);
            MaterialRatingBar bar = review.findViewById(R.id.gym_details_rate_write);
            bar.setRating(rating);
            TextInputEditText reviewMessage = review.findViewById(R.id.gym_details_review);
            ImageView profilePics = review.findViewById(R.id.profile_pic);
            if (ProfilePicHelper.getProfilePic() != null) profilePics.setImageDrawable(ProfilePicHelper.getProfilePic());
            else ProfilePicHelper.getProfilePicUpdateListener().add(profilePics::setImageDrawable);
            new AlertDialog.Builder(gymBottomSheet.getContext()).setTitle("Feedback about Gym").setCancelable(false)
                    .setView(review).setPositiveButton("Submit", (dialog, which) -> submitReview(bar, reviewMessage, gymListViewModel.getSelectedGym().getValue().getProperties().getINC_CRC()))
                    .setNeutralButton(android.R.string.cancel, (dialog, which) -> {
                flagReviewing = true;
                ratingBar.setRating(0);
            }).show();
        });

        // On Clicks
        gymLocation.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?daddr=" + ((coordinates == null) ?
                gymLocation.getText().toString() : (coordinates.latitude + "," + coordinates.longitude))))));
        carpark.setOnClickListener(view -> {
            GymList.GymShell gym = gymListViewModel.getSelectedGym().getValue();
            if (gym == null) return;
            Intent i = new Intent(view.getContext(), CarparkAndSearchResultActivity.class);
            i.putExtra("carpark", true);
            i.putExtra("gym", gym.getProperties().getINC_CRC());
            startActivity(i);
        });
        favourite.setOnClickListener(v -> heartIcon.callOnClick());
        heartIcon.setOnClickListener(v -> {
            if (v instanceof FavButtonView) {
                FavButtonView heart = (FavButtonView) v;
                heart.onClick(v); // Execute existing view onclick listener
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (getActivity() != null && user != null) {
                    new UpdateGymFavourites(getActivity(), user.getUid(), gymListViewModel.getSelectedGym().getValue().getProperties().getINC_CRC(), heart.isChecked(), success -> {
                        if (success) Snackbar.make(coordinatorLayout, (heart.isChecked()) ? "Saved to favourites!" : "Removed from favourites!", Snackbar.LENGTH_SHORT).show();
                        else Snackbar.make(coordinatorLayout, (heart.isChecked()) ? "Failed to save to favourites. Try again later" : "Failed to remove from favourites. Try again later", Snackbar.LENGTH_SHORT).show();
                    }).execute();
                }
            }
        });

        // Setup observers
        gymListViewModel.getFavCount().observe(this, integer -> favCount.setText(getResources().getString(R.string.number_counter, integer)));
        gymListViewModel.getSelectedGym().observe(this, gymShell -> {
            if (gymShell == null) {
                gymBottomSheetBehavior.setHideable(true);
                gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                backStack.setEnabled(false);
            } else {
                backStack.setEnabled(true);
                gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                gymBottomSheetBehavior.setHideable(false);
                gymTitle.setText(gymShell.getProperties().getName());
                gymDesc.setText(gymShell.getProperties().getDescription());
                if (gymDesc.getText().toString().trim().isEmpty()) gymDesc.setText("No description available");
                gymLocation.setText(GymHelper.generateAddress(gymShell.getProperties()));
                coordinates = new LatLng(gymShell.getGeometry().getLat(), gymShell.getGeometry().getLng());
                heartIcon.setChecked(false);

                DocumentReference gymRef = FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).document(gymShell.getProperties().getINC_CRC());
                HashMap<String, Integer> currentUserFavList = gymListViewModel.getCurrentUserFavourites().getValue();
                if (currentUserFavList.size() > 0 && currentUserFavList.containsKey(gymShell.getProperties().getINC_CRC())) {
                    heartIcon.setChecked(true);
                    gymListViewModel.updateFavCount(currentUserFavList.get(gymShell.getProperties().getINC_CRC()));
                } else
                    gymRef.get().addOnSuccessListener(documentSnapshot -> gymListViewModel.updateFavCount((documentSnapshot.exists()) ? Integer.parseInt(documentSnapshot.get("count").toString()) : 0))
                            .addOnFailureListener(e -> favCount.setText("(?)"));

                // Register update
                if (gymDetailFavListener != null) gymDetailFavListener.remove();
                gymDetailFavListener = gymRef.addSnapshotListener((documentSnapshot, e) -> gymListViewModel.updateFavCount((documentSnapshot != null && documentSnapshot.exists()) ?
                        Integer.parseInt(documentSnapshot.get("count").toString()) : 0));
                detailsInit = true;

                // DEBUG SETTINGS
                if (getContext() != null) {
                    LinearLayout debugView = gymBottomSheet.findViewById(R.id.gym_details_debug_layout);
                    TextView gymDetails = gymBottomSheet.findViewById(R.id.gym_details_debug_value);
                    gymDetails.setText(gymShell.getProperties().getINC_CRC());
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                    debugView.setVisibility((sp.getBoolean("debug_mode", false) ? View.VISIBLE : View.GONE));
                }

                updateGymRatings(gymShell.getProperties().getINC_CRC());
            }
        });
        gymListViewModel.getCurrentUserFavourites().observe(this, currentUserFavList -> {
            if (currentUserFavList.size() == 0) emptyFavourites();
            else {
                ArrayList<FavGymObject> finalList = new ArrayList<>();
                HashMap<String, GymList.GymShell> gymDetailsList = new HashMap<>();
                GymList gymList = GymHelper.getGymList(getContext());
                if (gymList == null) {
                    Log.e(TAG, "Failed to get gym list");
                    emptyFavourites(); // Error occurred
                    return;
                }
                for (GymList.GymShell g : gymList.getGyms()) gymDetailsList.put(g.getProperties().getINC_CRC(), g);

                for (String id : currentUserFavList.keySet()) {
                    if (gymDetailsList.containsKey(id)) finalList.add(new FavGymObject(gymDetailsList.get(id), currentUserFavList.get(id), 0.0f, 0));
                    else Log.e(TAG, "Unknown Gym (" + id + ")");
                }
                favAdapter = new FavGymAdapter(finalList);
                favAdapter.setOnClickListener(v -> {
                    if (v.getTag() instanceof FavGymAdapter.FavViewHolder) {
                        FavGymAdapter.FavViewHolder vh = (FavGymAdapter.FavViewHolder) v.getTag();
                        gymListViewModel.setSelectedGym((vh.getAdapterPosition() == -1) ? null : favAdapter.getList().get(vh.getAdapterPosition()));
                    }
                });
                favouritesList.setAdapter(favAdapter);

                // Get the list of gyms with reviews and update the list after
                Log.d(TAG, "Retrieving Gym Reviews to determine ratings");
                FirebaseFirestore.getInstance().collection(GymHelper.GYM_REVIEWS_COLLECTION).get().addOnSuccessListener(querySnapshot1 -> {
                    Log.d(TAG, "Gym Reviews for Favourites obtained. Size: " + querySnapshot1.getDocuments().size());
                    HashMap<String, GymRatingStats> refs = new HashMap<>();
                    for (DocumentSnapshot ds : querySnapshot1.getDocuments()) { refs.put(ds.getId(), ds.toObject(GymRatingStats.class)); }

                    List<FavGymObject> favGyms = favAdapter.getList();
                    for (FavGymObject fg : favGyms) {
                        if (refs.containsKey(fg.getGym().getProperties().getINC_CRC())) {
                            GymRatingStats gs = refs.get(fg.getGym().getProperties().getINC_CRC());
                            if (gs == null) continue;
                            fg.setRatingCount(gs.getCount());
                            fg.setAvgRating(gs.getAverageRating());
                        }
                    }
                    favAdapter.updateList(favGyms);
                    favAdapter.notifyDataSetChanged();
                }).addOnFailureListener(Throwable::printStackTrace);
            }
        });

        // Handle back press (if in gym details mode should revert)
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backStack);

        return root;
    }

    /**
     * Internal function to set the logged in user's gym review status
     * This will determine if the user has a review for that particular gym or not.
     * If there is, the view review layout is used, otherwise the create new review layout is used
     *
     * @param hasReview User currently has a review for the selected gym if true, false otherwise
     * @param message Any review message in the user's review of the gym, null for empty
     * @param rating The rating for the gym given by the user. Will be 0 by default
     * @param selectedGymUid Selected Gym UID
     */
    private void setGymStatus(boolean hasReview, @androidx.annotation.Nullable String message, float rating, String selectedGymUid) {
        LinearLayout edit = gymBottomSheet.findViewById(R.id.gym_details_rate_edit);
        LinearLayout view = gymBottomSheet.findViewById(R.id.gym_details_rate_view);
        Transition transition = new Fade();
        transition.setDuration(300);
        transition.addTarget(edit).addTarget(view);

        TransitionManager.beginDelayedTransition((ViewGroup) gymBottomSheet, transition);
        edit.setVisibility((hasReview) ? View.GONE : View.VISIBLE);
        view.setVisibility((hasReview) ? View.VISIBLE : View.GONE);
        if (hasReview) {
            // Update view mode
            TextView review = view.findViewById(R.id.gym_details_review_readonly);
            AppCompatRatingBar ratingBar = view.findViewById(R.id.gym_details_rate_read);
            ratingBar.setRating(rating);
            review.setText((message == null) ? "" : message);

            // We set the onclick here as only here we can easily access the message and rating
            Button editReview = gymBottomSheet.findViewById(R.id.gym_details_rate_edit_btn);
            editReview.setOnClickListener(v -> {
                View review1 = getLayoutInflater().inflate(R.layout.dialog_review, null);
                MaterialRatingBar bar = review1.findViewById(R.id.gym_details_rate_write);
                bar.setRating(rating);
                TextInputEditText reviewMessage = review1.findViewById(R.id.gym_details_review);
                reviewMessage.setText(message);
                ImageView profilePics = review1.findViewById(R.id.profile_pic);
                if (ProfilePicHelper.getProfilePic() != null) profilePics.setImageDrawable(ProfilePicHelper.getProfilePic());
                else ProfilePicHelper.getProfilePicUpdateListener().add(profilePics::setImageDrawable);
                new AlertDialog.Builder(gymBottomSheet.getContext()).setTitle("Feedback about Gym").setCancelable(false)
                        .setView(review1).setPositiveButton("Submit", (dialog, which) -> submitReview(bar, reviewMessage, selectedGymUid)).setNegativeButton(android.R.string.cancel, null)
                        .setNeutralButton("Delete", (dialog, which) -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) { Snackbar.make(gymBottomSheet, "Error deleting review. Please relogin", Snackbar.LENGTH_LONG).show(); return; }
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference ref = db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid).collection(GymHelper.GYM_USERS_COLLECTION).document(user.getUid());
                            ref.delete().addOnSuccessListener(aVoid -> {
                                Snackbar.make(gymBottomSheet, "Review deleted successfully!", Snackbar.LENGTH_LONG).show();
                                setGymStatus(false, null, 0f, selectedGymUid);
                                flagReviewing = true;
                                ((MaterialRatingBar) gymBottomSheet.findViewById(R.id.gym_details_rate_write)).setRating(0);
                            }).addOnFailureListener(e -> Snackbar.make(gymBottomSheet, "Failed to delete review (" + e.getLocalizedMessage() + ")", Snackbar.LENGTH_LONG).show());
                        }).show();
            });
        }

        // Update the rating after a delay
        new Handler().postDelayed(() -> FirebaseFirestore.getInstance().collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid).get().addOnSuccessListener(documentSnapshot -> {
            GymRatingStats stats = documentSnapshot.toObject(GymRatingStats.class);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (stats == null || user == null) return;
            AppCompatRatingBar rate = gymBottomSheet.findViewById(R.id.gym_details_rate_bar);
            TextView rating2 = gymBottomSheet.findViewById(R.id.gym_details_rate_avg);
            TextView count = gymBottomSheet.findViewById(R.id.gym_details_review_count_general);
            rate.setRating(stats.getAverageRating());
            rating2.setText(String.format(Locale.US,"%.2f", stats.getAverageRating()));
            count.setText("(" + stats.getCount() + ")");

            // Update favourites list as well
            FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).whereArrayContains("userIds", user.getUid()).get().addOnSuccessListener(this::processFavListUpdates);
        }), 5000); // Update after 5 seconds
    }

    /**
     * Internal function to submit review for the selected gym
     * @param bar The rating bar object that stores the gym rating that the user has selected
     * @param reviewMessage The review message TextInputEditText object that stores any review of the gym that the user is currently rating
     * @param selectedGymUid Selected Gym UID
     */
    private void submitReview(MaterialRatingBar bar, TextInputEditText reviewMessage, String selectedGymUid) {
        float rateValue = bar.getRating();
        String reviewValue = (reviewMessage.getText() == null) ? "" : reviewMessage.getText().toString().trim();
        // Attempt to detect any error messsages
        String error = (reviewValue.length() > 512) ? "Review message is too long" : null;
        FirestoreRating frate = new FirestoreRating(rateValue, reviewValue, System.currentTimeMillis());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { Snackbar.make(gymBottomSheet, "Error submitting review. Please relogin", Snackbar.LENGTH_LONG).show(); return; }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid).collection(GymHelper.GYM_USERS_COLLECTION).document(user.getUid());
        ref.set(frate).addOnSuccessListener(aVoid -> {
            Snackbar.make(gymBottomSheet, "Review submitted successfully!", Snackbar.LENGTH_LONG).show();
            // Update the main view as well and replace edit mode with view mode
            setGymStatus(true, reviewValue, rateValue, selectedGymUid);
        }).addOnFailureListener(e -> {
            Snackbar.make(gymBottomSheet, "Failed to submit review (" + ((error == null) ? e.getLocalizedMessage() : error) + ")", Snackbar.LENGTH_LONG).show();
            Log.e(TAG, "Failed to submit review (" + e.getLocalizedMessage() + ")");
        });
    }

    /**
     * Internal function to update gym ratings for a selected gym
     * @param selectedGymUid Selected Gym UID
     */
    private void updateGymRatings(String selectedGymUid) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not logged in, cannot get ratings and stuff");
            return; // We stop here
        }
        setGymStatus(false, null, 0f, selectedGymUid); // Default to no reviews while we are downloading
        MaterialRatingBar bar = gymBottomSheet.findViewById(R.id.gym_details_rate_write);
        if (bar.getRating() > 0f) {
            flagReviewing = true;
            bar.setRating(0);
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Get your user's rating list if any
        db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid).collection(GymHelper.GYM_USERS_COLLECTION).document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) setGymStatus(false, null, 0f, selectedGymUid); // No reviews from user
            else {
                FirestoreRating rating = documentSnapshot.toObject(FirestoreRating.class);
                if (rating != null) setGymStatus(true, rating.getMessage(), rating.getRating(), selectedGymUid);
                else Toast.makeText(getContext(), "Failed to get your ratings (ObjectCastError)", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to get ratings (" + e.getLocalizedMessage() + ")", Toast.LENGTH_LONG).show());
        // Set loading status for review while we retrieve the needed data (double calls boo)
        String[] loading = {"Loading Reviews for gym..."};
        StringRecyclerAdapter adapter = new StringRecyclerAdapter(Arrays.asList(loading), false);
        reviews.setAdapter(adapter);

        // Init for review count, rating and list
        TextView reviewCount = gymBottomSheet.findViewById(R.id.gym_details_review_count);
        TextView reviewCountGen = gymBottomSheet.findViewById(R.id.gym_details_review_count_general);
        AppCompatRatingBar overallRating = gymBottomSheet.findViewById(R.id.gym_details_rate_bar);
        TextView averageRating = gymBottomSheet.findViewById(R.id.gym_details_rate_avg);
        reviewCount.setText("(...)");
        reviewCountGen.setText("(...)");
        overallRating.setRating(0f);
        averageRating.setText("-");

        if (getActivity() == null) return;
        db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid).collection(GymHelper.GYM_USERS_COLLECTION).orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> reviewDocList = querySnapshot.getDocuments();
                    if (reviewDocList.size() <= 0) {
                        // No reviews
                        String[] noReviews = {"No Reviews Found for this gym. Make one now!"};
                        adapter.updateStrings(Arrays.asList(noReviews));
                        adapter.notifyDataSetChanged();
                        reviewCount.setText("(0)");
                        reviewCountGen.setText("(0)");
                        return;
                    }
                    // Get user list as well
                    db.collection(GymHelper.GYM_USERS_COLLECTION).get().addOnSuccessListener(querySnapshot1 -> {
                        if (getActivity() == null) return; // No more visible activity, no point doing
                        HashMap<String, User> userList = new HashMap<>();
                        for (DocumentSnapshot d : querySnapshot1.getDocuments()) { userList.put(d.getId(), d.toObject(User.class)); } // Parse user objects into hashmap

                        // Create the recycler items based off the reviews and users data
                        ArrayList<GymRatings> reviewList = new ArrayList<>();
                        float overallGymRating = 0.0f;
                        for (DocumentSnapshot d : reviewDocList) {
                            if (!userList.containsKey(d.getId())) continue; // Deleted user, Don't bother with their reviews
                            FirestoreRating fr = d.toObject(FirestoreRating.class);
                            User u = userList.get(d.getId());
                            if (fr == null || u == null) continue; // Error occurred, dont add that
                            reviewList.add(new GymRatings(d.getId(), fr, u));
                            overallGymRating += fr.getRating();
                        }

                        GymReviewAdapter reviewAdapter = new GymReviewAdapter(getActivity(), reviewList);
                        reviews.setAdapter(reviewAdapter);
                        reviewCount.setText("(" + reviewList.size() + ")");
                        reviewCountGen.setText("(" + reviewList.size() + ")");
                        // Get overall rating and stuff
                        float averageGymRating = overallGymRating / reviewList.size();
                        overallRating.setRating(averageGymRating);
                        averageRating.setText(String.format(Locale.US,"%.2f", averageGymRating));
                    }).addOnFailureListener(e -> { Log.e(TAG, "Failed to get review users"); Toast.makeText(getContext(), "Failed to get rating list (users)", Toast.LENGTH_LONG).show(); });
                }).addOnFailureListener(e -> { Toast.makeText(getContext(), "Failed to get rating list (list)", Toast.LENGTH_LONG).show(); Log.e(TAG, "Failed to get gym review list"); });
    }

    /**
     * Callback handler for Jetpack Navigation back override
     */
    private OnBackPressedCallback backStack = new OnBackPressedCallback(false) {
        /**
         * Function to execute when back button pressed
         */
        @Override
        public void handleOnBackPressed() {
            if (gymListViewModel.getSelectedGym().getValue() != null) gymListViewModel.setSelectedGym(null);
        }
    };

    /**
     * Lifecycle event called when the fragment is resumed
     */
    @Override
    public void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && favListener == null) {
            Query userFavGymQuery = FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).whereArrayContains("userIds", user.getUid());

            userFavGymQuery.get().addOnSuccessListener(this::processFavListUpdates);
            favListener = userFavGymQuery.addSnapshotListener((querySnapshot, e) -> processFavListUpdates(querySnapshot));
        }
    }

    /**
     * Lifecycle event called when the fragment is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        if (favListener != null) favListener.remove();
        favListener = null;
    }

    /**
     * Process real-time updates from Firebase Firestore
     * @param querySnapshot The database document snapshot at that current point in time
     */
    private void processFavListUpdates(QuerySnapshot querySnapshot) {
        Log.d(TAG, "processFavListUpdates()");
        // Update favourites
        if (querySnapshot != null && querySnapshot.size() > 0) {
            List<DocumentSnapshot> gyms = querySnapshot.getDocuments();
            HashMap<String, Integer> workingSet = new HashMap<>();
            for (DocumentSnapshot docs : gyms) {
                workingSet.put(docs.getId(), Integer.parseInt(Objects.requireNonNull(docs.get("count")).toString()));
            }
            gymListViewModel.updateCurrentUserFavourites(workingSet);
        } else gymListViewModel.updateCurrentUserFavourites(new HashMap<>());
        if (swipeToRefresh.isRefreshing()) swipeToRefresh.setRefreshing(false);
    }

    /**
     * Internal function to call when the user has no favourited gyms
     */
    private void emptyFavourites() {
        String[] toremove = {"No Favourited Gyms Saved"};
        StringRecyclerAdapter adapter = new StringRecyclerAdapter(Arrays.asList(toremove));
        favouritesList.setAdapter(adapter);
        favAdapter = null;
    }

    /**
     * When a gym in the favourites list has been swiped to unfavourite
     * @param position The position of the item being unfavourited
     * @return false if no errors
     */
    @Override
    public boolean delete(@Nullable Integer position) {
        // Unfavourite selected listener
        if (favAdapter == null || position == null) return false;
        String gymId = favAdapter.getList().get(position).getGym().getProperties().getINC_CRC();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (getActivity() != null && user != null) {
            new UpdateGymFavourites(getActivity(), user.getUid(), gymId, false, success -> {
                if (success) Snackbar.make(coordinatorLayout, "Removed from favourites!", Snackbar.LENGTH_SHORT).setAction("UNDO", v -> {
                    // Restore from favourites
                    new UpdateGymFavourites(getActivity(), user.getUid(), gymId, true, success1 -> {
                        if (success1) Snackbar.make(coordinatorLayout, "Removal from favourites undone", Snackbar.LENGTH_SHORT).show();
                        else Snackbar.make(coordinatorLayout, "Failed to undo favourites removal. Please refavourite the gym manually", Snackbar.LENGTH_SHORT).show();
                    }).execute(); }).show();
                else {
                    Snackbar.make(coordinatorLayout, "Failed to remove from favourites. Try again later", Snackbar.LENGTH_SHORT).show();
                }
            }).execute();
        }
        return false;
    }
}