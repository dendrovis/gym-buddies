package sg.edu.ntu.scse.cz2006.gymbuddies.tasks

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.CarparkAvailability
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.LtaObject
import java.io.File
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * Internal IntentService for updating carpark availability
 * for sg.edu.ntu.scse.cz2006.gymbuddies.tasks in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-22
 */
class UpdateCarparkAvailabilityService : IntentService(TAG) {

    /**
     * Checks if there is internet connection
     *
     * @return Boolean true if present, false otherwise
     */
    private fun isInternetAvailable(): Boolean {
        var result = false
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            cm?.run { cm.activeNetworkInfo?.run { if (type == ConnectivityManager.TYPE_WIFI) { result = true } else if (type == ConnectivityManager.TYPE_MOBILE) { result = true } } }
        }
        return result
    }

    /**
     * Internal function that invokes when the service starts with an intent being passed in
     *
     * This function will also handle running the update check in the background
     * @param intent Intent? The intent passed in
     */
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val apikey = sp.getString("ltakey", "invalid")
        if (apikey == "invalid") {
            Log.e(TAG, "No LTA API Key, not continuing")
            return
        }

        // Check internet connection
        if (!isInternetAvailable()) {
            Log.e(TAG, "No internet connection detected, not continuing")
            return
        }

        var skip = 0
        val gson = Gson()
        val carparkList = ArrayList<CarparkAvailability>()
        Log.i(TAG, "Downloading latest data")
        var retry = 0
        while (true) {
            try {
                val url = "$LTA_URL$skip"
                val uri = URL(url)
                val conn = uri.openConnection() as HttpURLConnection
                conn.connectTimeout = TIMEOUT
                conn.readTimeout = TIMEOUT
                conn.requestMethod = "GET"
                conn.setRequestProperty("AccountKey", apikey)
                conn.connect()
                Log.d(TAG, "Connecting to $url")

                val data = conn.inputStream.bufferedReader().use { it.readLine() }

                val obj = gson.fromJson<LtaObject>(data, LtaObject::class.java)
                if (obj.value.size == 0) {
                    Log.i(TAG, "Finished parsing data, exiting loop")
                    break
                }
                carparkList.addAll(obj.value)
                skip += obj.value.size
                retry = 0
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Failed to parse GSON from returned data")
                retry++
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, "HTTP Request Timeout (${e.localizedMessage})")
                retry++
            } finally {
                if (retry > 10) {
                    Log.e(TAG, "Failed query for getting availability from count $skip more than 10 times, failing and exiting service")
                    return
                }
            }
        }

        // Write to file
        Log.i(TAG, "Writing carpark list to file, Availability Size: ${carparkList.size}")
        val jsonString = gson.toJson(carparkList)
        val jsonFile = File(cacheDir, "avail.txt")
        jsonFile.writeText(jsonString)
    }

    /**
     * Internal lifecycle function called when the service is started
     */
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Started Service Work")
    }

    /**
     * Internal lifecycle function called when the service is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Finished all work")
    }

    companion object {
        /**
         * Internal TAG object
         */
        private const val TAG = "UpdateCarparkAvail"
        /**
         * LTA API URL
         */
        private const val LTA_URL = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2?\$skip="
        /**
         * HTTP Timeout value
         */
        private const val TIMEOUT = 15000 // Timeout 15 seconds

        /**
         * Function used to start the service to update carpark availability data
         * @param context Context The application Context
         */
        @JvmStatic
        fun updateCarpark(context: Context) {
            // Update carpark availability
            Log.i(TAG, "Updating carpark availability data")
            val intent = Intent(context, UpdateCarparkAvailabilityService::class.java)
            context.startService(intent)
        }
    }

}