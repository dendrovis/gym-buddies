package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper
import java.lang.ref.WeakReference

/**
 * Task to run the first run check in the background
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-16
 * @property callback Callback Callback to call when task completes
 * @property actRef WeakReference<(android.app.Activity..android.app.Activity?)> A weak reference to the activity
 * @constructor Initializees the task to execute
 */
class CheckFirstRun(activity: Activity, private val callback: Callback) : AsyncTask<String, Void, Void>() {

    private val actRef = WeakReference(activity)

    /**
     * Internal interface to handle callbacks
     */
    interface Callback {
        /**
         * Callback function called when returning results
         * @param success Boolean true if user does not have a profile, false otherwise
         */
        fun isFirstRun(success: Boolean)

        /**
         * Function called when an error occurs
         */
        fun isError()
    }

    /**
     * Internal task to execute in the background on a seperate thread
     * @param p0 Array<out String?> User ID
     * @return Void? No return value
     */
    override fun doInBackground(vararg p0: String?): Void? {
        val activity = actRef.get() ?: return null
        if (p0.isEmpty()) {
            Log.e(TAG, "No UID passed")
            activity.runOnUiThread { callback.isError() }
            return null
        }

        val uid = p0[0] as String
        Log.d(TAG, "Checking uid $uid")
        Log.i(TAG, "Obtaining user object")

        val firebaseDb = FirebaseFirestore.getInstance()
        val debugStart = System.currentTimeMillis()
        Log.d(TAG, "Time Taken Data Processing Start: $debugStart")
        firebaseDb.collection(GymHelper.GYM_USERS_COLLECTION).document(uid).get().addOnSuccessListener {
            Log.i(TAG, "User object retrieved, checking existance")
            val debugMid = System.currentTimeMillis()
            Log.d(TAG, "Time Taken Data Processing Middle: $debugMid")
            if (it.exists()) {
                Log.i(TAG, "User exists, checking if firstRun")
                val flags = it.toObject(User::class.java) // Default no
                val debugEnd = System.currentTimeMillis()
                Log.d(TAG, "Time Taken Data Processing End: $debugEnd")
                Log.d(TAG, "[Calculation] Total: ${debugEnd - debugStart}ms | S -> M: ${debugMid - debugStart}ms | M -> E: ${debugEnd - debugMid}ms")
                if (flags == null || flags.flags.firstRun) doCallback(true, activity) // User exists but has not completed first run for some reason
                else {
                    // User exists and completed first run
                    if (flags.uid.isEmpty()) {
                        // Update user UID object silently
                        flags.uid = uid
                        firebaseDb.collection(GymHelper.GYM_USERS_COLLECTION).document(uid).set(flags) // Silent so we do not care about results
                    }
                    doCallback(false, activity)
                }
            } else { Log.i(TAG, "User does not exist, creating new user"); Log.d(TAG, "[Calculation] Total: ${debugMid - debugStart}ms"); doCallback(true, activity) } // New User
        }.addOnFailureListener { Log.w(TAG, "Error getting Firebase Collection", it); activity.runOnUiThread { callback.isError() } } // Error, Fail it
        return null
    }

    /**
     * Sends a callback back to the calling [activity]
     * @param success Boolean true if user does not have a profile, false otherwise
     * @param activity Activity The calling activity
     */
    private fun doCallback(success: Boolean, activity: Activity) {
        Log.d(TAG, "callback:$success")
        activity.runOnUiThread { callback.isFirstRun(success) }
    }

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "CheckFirstRun"
    }

}