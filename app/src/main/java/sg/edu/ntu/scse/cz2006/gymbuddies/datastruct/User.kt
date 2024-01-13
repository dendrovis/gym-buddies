package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

import com.google.firebase.firestore.Exclude
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User.Flags
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User.PrefDays

/**
 * Data Structure containing the user object from the Firebase Firestore document object
 * For sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-16
 * @property name String User name
 * @property uid String User Identifier (this is obtained from FirebaseAuth#getUID())
 * @property prefLocation String User Preferred Location
 * @property gender String User Gender
 * @property prefTime String User Preferred Time Range
 * @property prefDay [PrefDays] User Preferred Days
 * @property profilePicUri String User Profile Picture URL
 * @property flags [Flags] User Flags
 * @constructor Creates a user object based on the Firebase Firestore document
 */
data class User(var name: String = "", var uid: String = "", var prefLocation: String = "West", var gender: String = "Male", var prefTime: String = "AM",
                var prefDay: PrefDays = PrefDays(), var profilePicUri: String = "", var flags: Flags = Flags()) {
    /**
     * Inner class to display user flags
     *
     * @author Kenneth Soh
     * @since 2019-09-16
     * @property firstRun Boolean true if it is the user's first time on the app
     * @constructor Part of the [User] object. See that for more information
     */
    data class Flags(var firstRun: Boolean = true)

    /**
     * Inner class to store preferred days of the user
     *
     * @author Kenneth Soh
     * @since 2019-09-16
     * @property monday Boolean Preferred State for Monday. True if preferred, false otherwise
     * @property tuesday Boolean Preferred State for Tuesday. True if preferred, false otherwise
     * @property wednesday Boolean Preferred State for Wednesday. True if preferred, false otherwise
     * @property thursday Boolean Preferred State for Thursday. True if preferred, false otherwise
     * @property friday Boolean Preferred State for Friday. True if preferred, false otherwise
     * @property saturday Boolean Preferred State for Saturday. True if preferred, false otherwise
     * @property sunday Boolean Preferred State for Sunday. True if preferred, false otherwise
     * @constructor Part of the [User] object. See that for more information
     */
    data class PrefDays(var monday: Boolean = false, var tuesday: Boolean = false, var wednesday: Boolean = false,
                        var thursday: Boolean = false, var friday: Boolean = false, var saturday: Boolean = false, var sunday: Boolean = false) {
        /**
         * A constructor that handles the init by passing an arraylist of the preferred days instead of individual
         * @param list ArrayList<Int> List of all days that are preferred
         * @constructor Part of the [User] object. See that for more information
         */
        constructor(list: ArrayList<Int>) : this() {
            list.forEach {
                when (it) {
                    1 -> monday = true
                    2 -> tuesday = true
                    3 -> wednesday = true
                    4 -> thursday = true
                    5 -> friday = true
                    6 -> saturday = true
                    7 -> sunday = true
                }
            }
        }

        /**
         * Gets the list of days preferred by the user
         * @return ArrayList<Int> List of days preferred by the user
         */
        @Exclude fun getDays(): ArrayList<Int> {
            val list = ArrayList<Int>()
            // Add accordingly (1 - Mon, 2 - Tues ... 7 - Sun
            if (monday) list.add(1)
            if (tuesday) list.add(2)
            if (wednesday) list.add(3)
            if (thursday) list.add(4)
            if (friday) list.add(5)
            if (saturday) list.add(6)
            if (sunday) list.add(7)
            return list
        }

        /**
         * Gets the days preferred by the user in a CSV format
         * @return String CSV formatted preferred days by the user
         */
        @Exclude fun getDaysCSV(): String {
            return getDays().joinToString(",")
        }
    }
}