package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * This is used to display Gym Ratings in a RecyclewView, will make use of the [FirestoreRating] object to store the reviews itself
 * For sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-12
 * @property user String User whose review is being shown
 * @property ratingObj FirestoreRating Review object
 * @property userObj User User's object (MUST MATCH [user])
 * @constructor Returns the review and rating of a particular gym by a particular user
 */
data class GymRatings(val user: String = "", val ratingObj: FirestoreRating = FirestoreRating(), val userObj: User = User())