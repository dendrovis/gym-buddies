package sg.edu.ntu.scse.cz2006.gymbuddies

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.children
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile_edit.*
import kotlinx.android.synthetic.main.row_pref_days.*
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateFirebaseFirestoreDocument
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UploadProfilePic
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper
import sg.edu.ntu.scse.cz2006.gymbuddies.util.InputHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * The activity that is used to handle profile view/edit for a logged in user
 * There are a couple of flags that are used here:
 * - [firstRun] denotes if the user is a new user and has not setup the application yet
 * - [editMode] denotes if the user is in a view-only mode or edit mode
 *
 * There are a couple more properties
 * - [uid] is the unique ID of the user
 * - [profileImage] and [profileUri] both defines the current user's profile picture if any
 * For sg.edu.ntu.scse.cz2006.gymbuddies in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-16
 * @property firstRun Boolean If this is the user's first logon to the app
 * @property profileImage Bitmap? The profile image if any of the user
 * @property profileUri Uri? The url to the profile image of the user if any
 * @property uid String The Unique ID of the user
 * @property editMode Boolean If the user is currently allowed to edit the profile
 * @property editUser User? The user in which this profile is editing
 */
class ProfileEditActivity : AppCompatActivity() {

    private var firstRun = false
    private var profileImage: Bitmap? = null
    private var profileUri: Uri? = null
    private lateinit var uid: String

    /**
     * Function that is called when an activity is created
     * @param savedInstanceState Bundle? The Android saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        firstRun = intent.getBooleanExtra("firstrun", false)
        Log.d(TAG, "FirstRun: $firstRun")

        // Assume auth always succeeed
        val auth = FirebaseAuth.getInstance().currentUser!!
        uid = auth.uid

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (firstRun) {
            supportActionBar?.title = "Create New Profile"
            Log.i(TAG, "Create new profile flow called")
            if (!auth.displayName.isNullOrEmpty()) etName.setText(auth.displayName)
            if (auth.photoUrl != null && auth.photoUrl.toString().toLowerCase(Locale.getDefault()) != "null") {
                // Set Uri of user
                profileUri = auth.photoUrl
                GetProfilePicFromFirebaseAuth(this, object: GetProfilePicFromFirebaseAuth.Callback {
                    override fun onComplete(bitmap: Bitmap?) {
                        // Update image drawable
                        profileImage = bitmap
                        val roundBitmap = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        roundBitmap.isCircular = true
                        profile_pic.setImageDrawable(roundBitmap)
                    }
                }).execute(profileUri)
            } else {
                Log.i(TAG, "No image found, showing default image")
                profile_pic.setImageResource(R.mipmap.ic_launcher) // Default Image
            }
        } else {
            if (intent.getBooleanExtra("view", false)) enterViewOnlyMode()
            loadImage.visibility = View.VISIBLE
            Toast.makeText(this, "Loading user data...", Toast.LENGTH_SHORT).show()
            FirebaseFirestore.getInstance().collection(GymHelper.GYM_USERS_COLLECTION).document(uid).get().addOnSuccessListener {
                loadImage.visibility = View.GONE
                if (it.exists()) {
                    editUser = it.toObject(User::class.java)
                    editUser?.let { user -> updateData(user) }
                }
            }.addOnFailureListener { loadImage.visibility = View.GONE; Log.e(TAG, "Error occurred retriving data for viewing")}
        }
        fab.setOnClickListener {
            Log.d(TAG, "FAB Clicked - Edit Mode: $editMode")
            if (editMode) {
                InputHelper.hideSoftKeyboard(this)
                // Check if uploading image
                if (loadImage.visibility == View.VISIBLE) Snackbar.make(coordinator, "Currently uploading profile picture. Wait for it to finish before saving profile", Snackbar.LENGTH_LONG).show()
                else {
                    if (validate()) addOrUpdate()
                    else Snackbar.make(coordinator, "Please complete your profile before continuing", Snackbar.LENGTH_LONG).show()
                }
            } else exitViewOnlyMode()
        }
        profile_pic.setOnClickListener {
            if (editMode) {
                val photoIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                startActivityForResult(photoIntent, REQUEST_PROFILE_PIC)
            }
        }
    }

    private var editMode: Boolean = true
    private var editUser: User? = null

    /**
     * This is an internal function of code to execute to enter view-only mode
     */
    private fun enterViewOnlyMode() {
        // Disables FAB and basically everything else
        Log.i(TAG, "Entering View Only Mode for Profile")
        supportActionBar?.title = "View Profile"
        profile_pic.isClickable = false
        etName.isEnabled = false
        location.isEnabled = false
        radio_gender.children.iterator().forEach { view -> view.isClickable = false }
        radio_time.children.iterator().forEach { view -> view.isClickable = false }
        cb_day1.isClickable = false
        cb_day2.isClickable = false
        cb_day3.isClickable = false
        cb_day4.isClickable = false
        cb_day5.isClickable = false
        cb_day6.isClickable = false
        cb_day7.isClickable = false
        tv_label_pref_days.visibility = View.GONE
        tv_label_pref_days_view.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_edit)
        tv_profile_pic_update_lbl.visibility = View.GONE
        editMode = false
    }

    /**
     * This is an internal function handling code to execute when exiting view-only mode and entering edit mode
     */
    private fun exitViewOnlyMode() {
        // Enter edit mode
        profile_pic.isClickable = true
        Log.i(TAG, "Exiting View Only Mode for Profile")
        supportActionBar?.title = "Edit Profile"
        etName.isEnabled = true
        location.isEnabled = true
        radio_gender.children.iterator().forEach { view -> view.isClickable = true}
        radio_time.children.iterator().forEach { view -> view.isClickable = true }
        tv_profile_pic_update_lbl.visibility = View.VISIBLE
        cb_day1.isClickable = true
        cb_day2.isClickable = true
        cb_day3.isClickable = true
        cb_day4.isClickable = true
        cb_day5.isClickable = true
        cb_day6.isClickable = true
        cb_day7.isClickable = true
        tv_label_pref_days.visibility = View.VISIBLE
        tv_label_pref_days_view.visibility = View.GONE
        fab.setImageResource(R.drawable.ic_save)
        editMode = true
    }

    /**
     * This is a function to handle updating a [user]'s data
     * @param user User The user to update data of
     */
    private fun updateData(user: User) {
        etName.setText(user.name)
        profileUri = Uri.parse(user.profilePicUri)
        GetProfilePicFromFirebaseAuth(this, object: GetProfilePicFromFirebaseAuth.Callback {
            override fun onComplete(bitmap: Bitmap?) {
                // Update image drawable
                profileImage = bitmap
                val roundBitmap = RoundedBitmapDrawableFactory.create(resources, bitmap)
                roundBitmap.isCircular = true
                profile_pic.setImageDrawable(roundBitmap)
            }
        }).execute(profileUri)
        val locations = resources.getStringArray(R.array.live_region)
        locations.forEachIndexed { index, s -> if (s == user.prefLocation) location.setSelection(index) }
        radio_gender.children.iterator().forEach { v -> if (v is RadioButton) { if (v.text.toString() == user.gender) v.isChecked = true } }
        radio_time.children.iterator().forEach { v -> if (v is RadioButton) { if (v.text.toString() == user.prefTime) v.isChecked = true } }
        cb_day1.isChecked = user.prefDay.monday
        cb_day2.isChecked = user.prefDay.tuesday
        cb_day3.isChecked = user.prefDay.wednesday
        cb_day4.isChecked = user.prefDay.thursday
        cb_day5.isChecked = user.prefDay.friday
        cb_day6.isChecked = user.prefDay.saturday
        cb_day7.isChecked = user.prefDay.sunday
    }

    /**
     * This is used to validate if all input requirements have been met
     * @return Boolean true if validated, false otherwise
     */
    private fun validate(): Boolean {
        // Check all fields set up and filled
        til_etName.isErrorEnabled = false
        val name = etName.text.toString().trim()
        val prefLocation = location.selectedItem.toString()
        val gender = findViewById<RadioButton>(radio_gender.checkedRadioButtonId).text
        val timeRange = findViewById<RadioButton>(radio_time.checkedRadioButtonId).text
        val selectedDays = getSelectedDays()
        Log.d(TAG, "Validating: \"$name\" | $prefLocation | $gender | $timeRange | Selected Days: ${selectedDays.joinToString(",")} (${selectedDays.size})")

        // Validation
        if (name.isEmpty()) {
            til_etName.error = "Please enter a valid name"
            til_etName.isErrorEnabled = true
            return false
        }
        if (selectedDays.isEmpty()) return false

        return true
    }

    /**
     * Internal function to handling the creation or update of user profile
     */
    private fun addOrUpdate() {
        Log.i(TAG, "Doing add/update of profile")
        val name = etName.text.toString().trim()
        val prefLocation = location.selectedItem.toString()
        val gender = findViewById<RadioButton>(radio_gender.checkedRadioButtonId).text.toString()
        val timeRange = findViewById<RadioButton>(radio_time.checkedRadioButtonId).text.toString()
        val selectedDays = getSelectedDays()
        val user = if (!firstRun && editUser != null) editUser!!.copy(name=name, prefLocation=prefLocation, gender=gender, prefTime=timeRange, prefDay=User.PrefDays(selectedDays), profilePicUri=profileUri.toString())
            else User(name=name, prefLocation = prefLocation, gender = gender, prefTime = timeRange, prefDay = User.PrefDays(selectedDays), profilePicUri = profileUri.toString()).apply { flags.firstRun = false }
        user.uid = uid // Set UID for the user object
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection(GymHelper.GYM_USERS_COLLECTION).document(uid)
        UpdateFirebaseFirestoreDocument(ref, user, object: UpdateFirebaseFirestoreDocument.Callback {
            override fun onComplete(success: Boolean) {
                Log.i(TAG, "Insertion Status: $success")
                if (success) {
                    val userProfileBuilder = UserProfileChangeRequest.Builder().apply {
                        setDisplayName(name)
                        if (user.profilePicUri.isNotEmpty()) setPhotoUri(Uri.parse(user.profilePicUri))
                    }
                    val currentUser = FirebaseAuth.getInstance().currentUser!!
                    Snackbar.make(coordinator, if (firstRun) "Creating Profile..." else "Updating Profile...", Snackbar.LENGTH_LONG).show()
                    currentUser.updateProfile(userProfileBuilder.build()).addOnCompleteListener {
                        currentUser.reload().addOnCompleteListener {
                            startActivity(Intent(this@ProfileEditActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    Snackbar.make(coordinator, "An error occurred updating profile", Snackbar.LENGTH_LONG).show()
                }
            }
        } ).execute()
    }

    /**
     * Internal function to obtain a list of preferred days that have been selected
     * @return ArrayList<Int> List of preferred days that are selected
     */
    private fun getSelectedDays(): ArrayList<Int> {
        val list = ArrayList<Int>()
        // Add accordingly (1 - Mon, 2 - Tues ... 7 - Sun
        if (cb_day1.isChecked) list.add(1)
        if (cb_day2.isChecked) list.add(2)
        if (cb_day3.isChecked) list.add(3)
        if (cb_day4.isChecked) list.add(4)
        if (cb_day5.isChecked) list.add(5)
        if (cb_day6.isChecked) list.add(6)
        if (cb_day7.isChecked) list.add(7)
        return list
    }

    /**
     * This is used to handle results from any provider based profile pic such as from the Google Authentication
     * @param requestCode Int Intent Request Code such as [REQUEST_PROFILE_PIC]
     * @param resultCode Int Intent Result code. Can be [Activity.RESULT_OK] or [Activity.RESULT_CANCELED]
     * @param data Intent? Any profile picture data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PROFILE_PIC && resultCode == Activity.RESULT_OK && data != null) {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(data.data as Uri))
            // Upload image into cloud before setting
            loadImage.visibility = View.VISIBLE
            val storage = FirebaseStorage.getInstance()
            val ref = storage.reference.child("profile/${uid}.jpg")
            UploadProfilePic(this, ref, bitmap, object: UploadProfilePic.Callback {
                override fun onSuccess(success: Boolean, imageUri: Uri?) {
                    if (success) {
                        // Update stuff
                        profileImage = bitmap
                        profileUri = imageUri
                        val roundBitmap = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        roundBitmap.isCircular = true
                        profile_pic.setImageDrawable(roundBitmap)
                    }
                    loadImage.visibility = View.GONE
                }
            }).execute()
        }
    }

    /**
     * This function overrides the back functionality of the activity
     */
    override fun onBackPressed() {
        if (firstRun) {
            // Log user out
            finish()
            val logout = Intent(this, LoginChooserActivity::class.java).apply { putExtra("logout", true) }
            startActivity(logout)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * This function handles the selected menu [item]
     * @param item MenuItem The item that has been selected
     * @return Boolean true if completed, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "ProfileEdit"
        /**
         * Intent to request for profile pictures
         */
        private const val REQUEST_PROFILE_PIC = 1
    }

}
