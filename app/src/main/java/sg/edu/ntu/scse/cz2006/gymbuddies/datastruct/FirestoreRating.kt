package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * This data structure is used to store the object that will be stored in the Firebase Firestore object ONLY
 * For sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-11
 * @property rating Float Rating value (1.0 - 5.0)
 * @property message String Review message of the gym
 * @property timestamp Long Last modified date of the review
 * @constructor Returns a rating object that can be stored/retrieved from Firebase Firestore
 */
data class FirestoreRating(var rating: Float = 0.0f, var message: String = "", val timestamp: Long = System.currentTimeMillis())