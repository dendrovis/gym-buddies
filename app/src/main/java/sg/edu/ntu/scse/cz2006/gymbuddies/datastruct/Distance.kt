package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

import com.google.android.gms.maps.model.MarkerOptions
import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarPark

/**
 * Internal data class for storing [distance] of a [marker] from another point
 * for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-21
 * @property distance Float Distance of the marker from another point
 * @property marker MarkerOptions? The marker in which the distance relates to
 * @property carpark CarPark? Carpark in which distance relates to
 * @constructor Creates an object of the [distance] of the [marker] in reference to another point
 */
data class Distance(var distance: Float = 0f, var marker: MarkerOptions? = null, var carpark: CarPark? = null)