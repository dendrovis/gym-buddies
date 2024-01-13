package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference

/**
 * Task ran to upload profile picture onto Firebase Storage and generate the URL of the image
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-16
 * @property ref StorageReference Reference to store the soon-to-be uploaded picture in on Firebase Storage
 * @property bitmap Bitmap The bitmap image to store on the server and generate the URL from
 * @property callback Callback Callback to call when task completes
 * @property actRef WeakReference<(android.app.Activity..android.app.Activity?)> A weak reference to the activity
 * @constructor Initializees the task to execute
 */
class UploadProfilePic(activity: Activity, private val ref: StorageReference, private val bitmap: Bitmap, private val callback: Callback) : AsyncTask<Void, Void, Void>() {

    private val actRef = WeakReference(activity)

    /**
     * Internal interface to handle callbacks
     */
    interface Callback {
        /**
         * Callback function called when the task completes with the [success] status and the [imageUri] of the image you just uploaded
         * @param success Boolean If you have succeeded in updating the profile picture
         * @param imageUri Uri? URL of the Uploaded Profile Picture
         */
        fun onSuccess(success: Boolean, imageUri: Uri? = null)
    }

    /**
     * Internal task to execute in the background on a seperate thread
     * @param p0 Array<out Void?> No arguements
     * @return Void? No return value
     */
    override fun doInBackground(vararg p0: Void?): Void? {
        val activity = actRef.get() ?: return null
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val task = ref.putBytes(data)

        task.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            Log.d(TAG, "Upload Progress: $progress%")
        }.addOnFailureListener {
            Log.e(TAG, "Upload Failed (${it.message})", it)
            activity.runOnUiThread { callback.onSuccess(false) }
        }.addOnSuccessListener {
            Log.i(TAG, "Upload Successful, getting Download URL")
            ref.downloadUrl.addOnFailureListener {
                Log.e(TAG, "Retrieving download url Failed (${it.message})", it)
                activity.runOnUiThread { callback.onSuccess(false) }
            }.addOnSuccessListener {
                Log.i(TAG, "Download URL Obtained")
                Log.d(TAG, "Download URL: $it")
                activity.runOnUiThread { callback.onSuccess(true, it) }
            }
        }
        return null
    }

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "UploadProfilePic"
    }
}