package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.content.Context
import com.google.gson.Gson
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList

/**
 * Gym Helper Class Object
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-01
 */
object GymHelper {

    /**
     * An internal property that stores parsed gym lists so that we do not need to parse it again
     */
    @JvmStatic private var gymList: GymList? = null

    /**
     * Gets the list of gym objects from the JSON file in the application
     * @param context Context Application Context
     * @return GymList? Gyms saved in the JSON file and parsed
     */
    @JvmStatic
    fun getGymList(context: Context): GymList? {
        if (gymList != null) return gymList
        val json = JsonHelper.readFromRaw(context, R.raw.gymlist)
        val gson = Gson()
        gymList = gson.fromJson(json, GymList::class.java)
        return gymList
    }

    /**
     * Get gym object if available from the application
     * @param context Context Application Context
     * @param gymId String Gym ID
     * @return GymList.GymShell? Gym object if any, null otherwise
     */
    @JvmStatic
    fun getGym(context: Context, gymId: String): GymList.GymShell? {
        val gym = getGymList(context) ?: return null
        gym.gyms.forEach {
            if (it.properties.INC_CRC == gymId) return it
        }
        return null
    }

    /**
     * Generates the gym address based on the gym properties [prop] that you have passed in
     * @param prop GymProperties The Gym's Properties object
     * @return String Full Address of the gym
     */
    @JvmStatic
    fun generateAddress(prop: GymList.GymProperties): String {
        val sb = StringBuilder()
        prop.ADDRESSBLOCKHOUSENUMBER?.let { sb.append("$it ") }
        prop.ADDRESSBUILDINGNAME?.let { sb.append("$it ") }
        sb.append("${prop.ADDRESSSTREETNAME} ")
        if (prop.ADDRESSFLOORNUMBER != null && prop.ADDRESSUNITNUMBER != null) {
            // Check for single digit
            var floorNo = prop.ADDRESSFLOORNUMBER
            var unitNo = prop.ADDRESSUNITNUMBER
            try { if (floorNo.toInt() < 10) floorNo = "0$floorNo" } catch (e: NumberFormatException) { } // Not an issue if it crashes
            try { if (unitNo.toInt() < 10) unitNo = "0$unitNo" } catch (e: NumberFormatException) { } // Not an issue if it crashes
            sb.append("#$floorNo-$unitNo ")
        }
        prop.ADDRESSPOSTALCODE.let { sb.append("S($it)")}
        return sb.toString()
    }

    /**
     * A constant for the gym collection in Firebase Firestore DB
     */
    const val GYM_COLLECTION = "favgym"
    /**
     * A constant for the gym review collection in Firebase Firestore DB
     */
    const val GYM_REVIEWS_COLLECTION = "gymreviews"
    /**
     * A constant for the gym user collection in Firebase Firestore DB
     */
    const val GYM_USERS_COLLECTION = "users"
}