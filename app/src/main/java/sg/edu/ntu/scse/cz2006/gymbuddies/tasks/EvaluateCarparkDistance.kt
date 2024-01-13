package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarPark
import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarParkDao
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.Distance
import sg.edu.ntu.scse.cz2006.gymbuddies.util.svy21converter.SVY21Coordinate

/**
 * Task to execute to generate carpark distances from the gym itself
 * For sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-25
 * @property gymLocation LatLng Current location of the gym
 * @property carpark List<CarPark> List of carparks
 * @property callback Callback callback to call when task completes
 * @constructor Initializees the task to execute
 */
class EvaluateCarparkDistance(private val gymLocation: LatLng, private val carparkDao: CarParkDao, private val callback: Callback) : AsyncTask<Void, Void, ArrayList<Pair<CarPark, Float>>>() {

    /**
     * Internal interface to handle callbacks
     */
    interface Callback {
        /**
         * Callback function called when the task completed with the list of [results] on carpark distances
         * @param results ArrayList<Pair<CarPark, Float>> The carpark distances
         */
        fun onComplete(results: ArrayList<Pair<CarPark, Float>>)
    }

    /**
     * Internal task to execute in the background on a seperate thread
     * @param p0 Array<out Void?> No arguements
     * @return ArrayList<Pair<CarPark, Float>> List of carpark distances
     */
    override fun doInBackground(vararg p0: Void?): ArrayList<Pair<CarPark, Float>> {
        val distances = ArrayList<Distance>()

        val carpark = carparkDao.allCarParksNow

        Log.d(TAG, "Carpark Count: ${carpark.size}")

        // Get distance from the current location
        Log.i(TAG, "Converting markers to positional value based on current user location")
        carpark.forEach {
            val result = FloatArray(1)
            val svyCoord = SVY21Coordinate(it.y, it.x)
            val latlng = svyCoord.asLatLon()
            //Log.d(TAG, "${it.id}: SVY21: ${svyCoord.easting}, ${svyCoord.northing}, LatLng: ${latlng.latitude}, ${latlng.longitude}")
            Location.distanceBetween(gymLocation.latitude, gymLocation.longitude, latlng.latitude, latlng.longitude, result)
            distances.add(Distance(result[0], carpark = it))
        }

        Log.d(TAG, "Carpark Count: ${distances.size}")
        distances.sortBy { it.distance }
        val result = ArrayList<Pair<CarPark, Float>>()
        distances.forEach{ result.add(Pair(it.carpark!!, it.distance)) }

        // Send intent
        return result
    }

    /**
     * Function ran when the task completes
     * @param result ArrayList<Pair<CarPark, Float>> List of all carpark distances to the gym
     */
    override fun onPostExecute(result: ArrayList<Pair<CarPark, Float>>) {
        super.onPostExecute(result)
        callback.onComplete(result)
    }

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "EvalCarparks"
    }
}