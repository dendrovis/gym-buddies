package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.Activity
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper
import java.lang.ref.WeakReference

/**
 * Task to parse the gym data file from the app resource
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-17
 * @property callback Callback Callback to call when task completes
 * @property actRef WeakReference<(android.app.Activity..android.app.Activity?)> A weak reference to the activity
 * @constructor Initializees the task to execute
 */
class ParseGymDataFile(activity: Activity, private val callback: Callback) : AsyncTask<Void, Void, Void>() {
    private val actRef = WeakReference(activity)

    /**
     * Internal interface to handle callbacks
     */
    interface Callback {
        /**
         * Callback function executed when the task completes with the corresponding [results]
         * @param results HashMap<MarkerOptions, GymShell>? Result in the form of a hashmap of Google Maps MarkerOptions and the list of all gyms
         */
        fun onComplete(results: HashMap<MarkerOptions, GymList.GymShell>?)
    }

    /**
     * Internal task to execute in the background on a seperate thread
     * @param p0 Array<out Void?> No arguements
     * @return Void? No return value
     */
    override fun doInBackground(vararg p0: Void?): Void? {
        val activity = actRef.get() ?: return null
        val gymlist = GymHelper.getGymList(activity)
        if (gymlist == null) {
            activity.runOnUiThread { callback.onComplete(null) }
            return null
        }
        val markers = HashMap<MarkerOptions, GymList.GymShell>()
        gymlist.gyms.forEach { markers[MarkerOptions().position(LatLng(it.geometry.getLat(), it.geometry.getLng())).title(it.properties.Name).snippet(GymHelper.generateAddress(it.properties))] = it }
        activity.runOnUiThread { callback.onComplete(markers) }
        return null
    }
}