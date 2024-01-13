package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Data Structure for storing the Firebase Firestore's Favourite Gym document objects
 * For sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-02
 * @property userIds ArrayList<String> List of users who favourited this gym
 * @property count Int Number of people who favourited this gym
 * @constructor Creates a object based on the Firebase Firestore document
 */
data class FavGymFirestore(var userIds: ArrayList<String> = ArrayList(), var count: Int = 0)