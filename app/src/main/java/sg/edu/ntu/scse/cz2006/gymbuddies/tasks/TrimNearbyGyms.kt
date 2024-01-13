package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Distance

/**
 * Task to execute to cut down the number of markers displayed on the map for gyms
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-25
 * @property count Int Number of markers to display only
 * @property location LatLng Current location of the user
 * @property markers Set<MarkerOptions> List of all markers to trim
 * @property callback Callback Callback to call when task completes
 * @constructor Initializees the task to execute
 */
class TrimNearbyGyms(private var count: Int, private val location: LatLng, private val markers: Set<MarkerOptions>, private val callback: Callback) : AsyncTask<Void, Void, ArrayList<MarkerOptions>>() {

    /**
     * Internal interface to handle callbacks
     */
    interface Callback {
        /**
         * Callback function called when the task completed with the list of [results] on trimmed markers
         * @param results ArrayList<MarkerOptions> The trimmed down Google Maps markers
         */
        fun onComplete(results: ArrayList<MarkerOptions>)
    }

    /**
     * Internal task to execute in the background on a seperate thread
     * @param p0 Array<out Void?> No arguements
     * @return ArrayList<MarkerOptions> List of trimmed Google Maps markers
     */
    override fun doInBackground(vararg p0: Void?): ArrayList<MarkerOptions> {
        val distances = ArrayList<Distance>()

        // Location.distanceBetween()
        // Get distance from the current location
        Log.i(TAG, "Converting markers to positional value based on current user location")
        markers.forEach {
            val result = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, it.position.latitude, it.position.longitude, result)
            distances.add(Distance(result[0], it))
        }

        // Sort the distances from smallest to largest
        Log.i(TAG, "Sorting distance")
        distances.sortBy { it.distance }

        // Select the first x amount determined by count
        Log.i(TAG, "Retrieving first $count gyms")
        if (count > distances.size) {
            Log.w(TAG, "Count from user preference ($count) is larger than the gym list (${distances.size}), defaulting to full list")
            count = distances.size
        }
        val resultSubList = distances.subList(0, count)
        val result = ArrayList<MarkerOptions>()
        resultSubList.forEach{ it.marker?.let{ marker -> result.add(marker) } }

        // Send intent
        return result
    }

    /**
     * Function ran when the task completes
     * @param result ArrayList<MarkerOptions> List of all trimmed Google Maps markers
     */
    override fun onPostExecute(result: ArrayList<MarkerOptions>) {
        super.onPostExecute(result)
        callback.onComplete(result)
    }

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "TrimNearbyGyms"
    }
}