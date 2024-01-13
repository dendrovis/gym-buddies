package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;


/**
 * Data Accessing Object for entity 'hdbcarparks' in the Room database.
 *
 * @author Chia Yu
 * @since 2019-09-14
 */
@Dao
public interface CarParkDao {

    /**
     * getting list of all car parks in room database
     */
    @Query("SELECT * FROM hdbcarparks")
    LiveData<List<CarPark>> getAllCarParks();

    /**
     * Get a list of all carparks from the database
     */
    @Query("SELECT * FROM hdbcarparks")
    List<CarPark> getAllCarParksNow();


}
