package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Shell object for the LTA Carpark Availiability API's Gson object
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-22
 * @property value ArrayList<CarparkAvailability> The actual object for Carpark Availability
 * @constructor This is just merely a shell containing the actual Carpark Availability object. Unfortunately required due to the way LTA crafted their API JSON response
 */
data class LtaObject(val value: ArrayList<CarparkAvailability> = ArrayList())