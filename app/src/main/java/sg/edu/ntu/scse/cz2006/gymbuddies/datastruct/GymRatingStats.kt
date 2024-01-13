package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Statistics for Gym Ratings. For use by recycler adapters in recyclerviews
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-14
 * @property averageRating Float Average Rating of Gym
 * @property count Int Number of Ratings for the Gym
 * @property totalRating Float Total Rating Score for gym (tabulated by summing up all the ratings in the gym)
 * @constructor Returns an object signifying the gym rating statistics for a particular gym
 */
data class GymRatingStats(val averageRating: Float = 0.0f, val count: Int = 0, val totalRating: Float = 0.0f)