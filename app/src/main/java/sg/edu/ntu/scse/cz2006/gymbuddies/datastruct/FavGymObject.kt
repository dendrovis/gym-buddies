package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Data Structure to store the Favourited Gym Object for use by the RecyclerView favourites list
 * For sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-02
 * @property gym GymShell Gym object
 * @property favCount Int The number of users who have favouited the gym object in [gym]
 * @constructor Creates a object for the Favourites List RecyclerView
 */
data class FavGymObject(val gym: GymList.GymShell, val favCount: Int = 0, var avgRating: Float = 0.0f, var ratingCount: Int = 0)