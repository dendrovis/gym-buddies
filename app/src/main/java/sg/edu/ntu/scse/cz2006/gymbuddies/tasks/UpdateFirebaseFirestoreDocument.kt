package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User

/**
 * This task handles updating of Firebase Firestore DB user document object in the background
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-16
 * @property docRef DocumentReference Document reference in the Firebase Firestore DB
 * @property userObj User User object to update
 * @property callback Callback Callback to call when task completes
 * @constructor Initializees the task to execute
 */
class UpdateFirebaseFirestoreDocument(private var docRef: DocumentReference, var userObj: User, private var callback: Callback) : AsyncTask<Void, Void, Boolean>() {

    /**
     * Internal task to execute in the background on a seperate thread
     * @param p0 Array<out Void?> No arguements
     * @return Boolean true if completed, false otherwise
     */
    override fun doInBackground(vararg p0: Void?): Boolean {
        docRef.set(userObj)
        return true
    }

    /**
     * Function executed after the task completes with the [result] of the task
     * @param result Boolean Result of the task
     */
    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (!result) {
            Log.w(TAG, "An error occurred")
            callback.onComplete(false)
        } else {
            callback.onComplete(true)
        }
    }

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

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "UpdateFbFsObj"
    }
}