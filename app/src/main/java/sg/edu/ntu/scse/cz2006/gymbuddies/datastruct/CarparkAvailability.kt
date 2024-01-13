package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Data Structure object to hold the Gson object for the LTA Carpark Availability API
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-22
 * @property CarParkID String Carpark UID
 * @property Area String Carpark Area (E.g Marina), only used for LTA carparks
 * @property Development String Carpark Address
 * @property Location String Coordinates of carpark
 * @property AvailableLots Int Number of available lots in the carpark at the point in time
 * @property LotType String Type of lot. Can be of [TYPE_CAR], [TYPE_HEAVY_VEH] or [TYPE_MOTOCYCLE]
 * @property Agency String Agency managing the carpark. Can be of [AGENCY_HDB], [AGENCY_LTA] or [AGENCY_URA]
 * @constructor
 */
data class CarparkAvailability(val CarParkID: String = "", val Area: String = "", val Development: String = "Nanyang Technological University Singapore", val Location: String = "1.3462 103.6820",
                               val AvailableLots: Int = 0, val LotType: String = TYPE_CAR, val Agency: String = AGENCY_HDB) {

    companion object {
        /**
         * Carpark is meant for cars
         */
        const val TYPE_CAR = "C"
        /**
         * Carpark is meant for heavy vehicles
         */
        const val TYPE_HEAVY_VEH = "H"
        /**
         * Carpark is meant for motocycles
         */
        const val TYPE_MOTOCYCLE = "Y"

        /**
         * Carpark is managed by Housing Development Board Singapore (HDB)
         */
        const val AGENCY_HDB = "HDB"
        /**
         * Carpark is managed by the Land Transport Authroity Singapore (LTA)
         */
        const val AGENCY_LTA = "LTA"
        /**
         * Carpark is managed by the Urban Redevelopment Authority Singapore (URA)
         */
        const val AGENCY_URA = "URA"
    }
}