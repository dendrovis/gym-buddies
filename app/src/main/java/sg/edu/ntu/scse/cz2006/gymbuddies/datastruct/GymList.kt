package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Data Structure to store the data from the gym JSON file
 * Note that this is just an outer shell. Access [gyms] for the actual data
 * For sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-17
 * @property name Name of the dataset
 * @property gyms ArrayList<GymShell> List of all gyms
 * @constructor Creates the object that is used to store the data from the JSON file
 */
data class GymList(val name: String = "", val gyms: ArrayList<GymShell> = ArrayList()) {
    /**
     * Inner class to handle the gym objects from the JSON file
     *
     * @author Kenneth Soh
     * @since 2019-09-17
     * @property properties GymProperties List of all gym properties
     * @property geometry GymGeometry LatLng data of the gym
     * @constructor Part of the [GymList] object. See that for more information
     */
    data class GymShell(val properties: GymProperties = GymProperties(), val geometry: GymGeometry = GymGeometry())

    /**
     * Inner class containing all of the gym properties
     *
     * @author Kenneth Soh
     * @since 2019-09-17
     * @property Name String Gym Name
     * @property description String Gym Description
     * @property altitudeMode String Altitude Mode defined for this gym
     * @property INC_CRC String Gym Unique ID
     * @property ADDRESSPOSTALCODE String Gym Postal Code
     * @property ADDRESSUNITNUMBER String? Gym Unit Number (if any)
     * @property ADDRESSBUILDINGNAME String? Gym Building Name (if any)
     * @property ADDRESSFLOORNUMBER String? Gym Floor Number (if any)
     * @property ADDRESSSTREETNAME String Gym Street Name (if any)
     * @property ADDRESSBLOCKHOUSENUMBER String? Gym Block House Number (if any)
     * @constructor Part of the [GymShell] object. See that for more information
     */
    data class GymProperties(val Name: String = "", val description: String = "", val altitudeMode: String = "clampToGround", val INC_CRC: String = "PRIMARYKEY",
                             val ADDRESSPOSTALCODE: String = "111111", val ADDRESSUNITNUMBER: String? = null, val ADDRESSBUILDINGNAME: String? = null,
                             val ADDRESSFLOORNUMBER: String? = null, val ADDRESSSTREETNAME: String = "Knn Francis Rd", val ADDRESSBLOCKHOUSENUMBER: String? = null) {
        /**
         * Gets the gym postal code as a Int
         * @return Int The Gym postal code in integer
         */
        fun getPostalCodeInt(): Int { return try { ADDRESSPOSTALCODE.toInt() } catch (e: NumberFormatException) { 0 } }
    }

    /**
     * Inner class containing the latitude and longitude of the gym location
     *
     * @author Kenneth Soh
     * @since 2019-09-17
     * @property coordinates ArrayList<Double> Coordinates of the gym. Index 0 provides latitude, 1 provides longitude
     * @constructor Part of [GymShell] obeject. See that for more information
     */
    data class GymGeometry(val coordinates: ArrayList<Double> = ArrayList()) {
        /**
         * Gets the latitude of the gym
         * @return Double Latitude of the gym
         */
        fun getLat(): Double { return if (coordinates.size > 1) coordinates[1] else 0.0 }

        /**
         * Gets the longitude of the gym
         * @return Double Longitude of the gym
         */
        fun getLng(): Double { return if (coordinates.size > 0) coordinates[0] else 0.0 }
    }
}