package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import sg.edu.ntu.scse.cz2006.gymbuddies.util.ProfilePicHelper
import java.lang.ref.WeakReference

/**
 * Task to retrieve profile picture from the Firebase Auth Provider
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-09
 * @property callback Callback Callback to call when task completes
 * @property actRef WeakReference<(android.app.Activity..android.app.Activity?)> A weak reference to the activity
 * @constructor Initializees the task to execute
 */
class GetProfilePicFromFirebaseAuth(activity: Activity, var callback: Callback) : AsyncTask<Uri, Void, Void>() {

    /**
     * Internal interface to handle callbacks
     */
    interface Callback {
        /**
         * Callback function called when the task completes with the [bitmap] downloaded
         * @param bitmap Bitmap? Profile picture retrieved. Can be null for empty
         */
        fun onComplete(bitmap: Bitmap?)
    }

    private val actRef = WeakReference<Activity>(activity)

    /**
     * Internal task to execute in the background on a seperate thread
     * @param imageUris Array<out Uri?> Image URL to retrieve the image from
     * @return Void? No return value
     */
    override fun doInBackground(vararg imageUris: Uri?): Void? {
        if (imageUris.isEmpty()) return null
        val act = actRef.get() ?: return null

        val bmp = ProfilePicHelper.getImageBitmap(imageUris[0].toString())
        act.runOnUiThread { callback.onComplete(bitmap = bmp) }

        return null
    }
}