package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper.GYM_COLLECTION
import java.lang.ref.WeakReference

/**
 * Task to handle updating gym favourites when the favourites button is clicked
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-02
 * @property userid String User ID of the user who clicked the button
 * @property gymId String? The gym being favourited or unfavourited
 * @property favStatus Boolean The toggle state (true if we are favouriting, false if you are unfavouriting)
 * @property callback Callback Callback to call when task completes
 * @property actRef WeakReference<(android.app.Activity..android.app.Activity?)> A weak reference to the activity
 * @constructor Initializees the task to execute
 */
class UpdateGymFavourites(activity: Activity, private val userid: String, private val gymId: String?, private val favStatus: Boolean, private val callback: Callback) : AsyncTask<Void, Void, Void>() {

    private val actRef = WeakReference(activity)

    /**
     * Internal interface to handle callbacks
     */
    interface Callback {
        /**
         * Callback function called when the task completes with its [success] value
         * @param success Boolean true if successful, false otherwise
         */
        fun onComplete(success: Boolean)
    }

    /**
     * Internal task to execute in the background on a seperate thread
     * @param p0 Array<out Void?> No arguements
     * @return Void? No return value
     */
    override fun doInBackground(vararg params: Void?): Void? {
        val activity = actRef.get() ?: return null
        if (gymId == null) {
            activity.runOnUiThread { callback.onComplete(false) }
            return null
        }
        val firebaseDb = FirebaseFirestore.getInstance()
        firebaseDb.collection(GYM_COLLECTION).document(gymId).get().addOnSuccessListener { snapshot ->
            Log.i(TAG, "Favourite Gym object retrieved, checking existance")
            val favGym: FavGymFirestore? = if (snapshot.exists()) {
                Log.i(TAG, "Favourite gym exists, updating favourites")
                snapshot.toObject(FavGymFirestore::class.java)
            } else {
                Log.i(TAG, "Gym was never favourited, creating new object")
                FavGymFirestore()
            }
            favGym?.let {
                val userList = it.userIds
                if (favStatus) {if (!userList.contains(userid)) userList.add(userid) } // Favourite if not inside already
                else userList.remove(userid) // Unfavourite
                it.count = userList.size
                Log.i(TAG, "Updating gym object. State: ${if (favStatus) "Added" else "Removed"}, Size: ${it.count}")
                firebaseDb.collection(GYM_COLLECTION).document(gymId).set(it).addOnSuccessListener { activity.runOnUiThread { callback.onComplete(true) } }
                    .addOnFailureListener { activity.runOnUiThread { callback.onComplete(false) } }
            } ?: activity.runOnUiThread { callback.onComplete(false) }
        }.addOnFailureListener { Log.e(TAG, "Error getting Firebase Collection", it); activity.runOnUiThread { callback.onComplete(false) } } // Error, Log it
        return null
    }

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "UpdateGymFavourites"
    }

}