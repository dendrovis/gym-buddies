package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database for Gym Buddy app
 * it uses singleton design pattern, it will only create one instance at all time
 *
 * schema information will be exported to '../app/schemas/' whenever database version update
 *
 * @author Chia Yu
 * @since 2019-09-14
 */
@Database(entities = {CarPark.class}, version=1, exportSchema = true)
public abstract class GBDatabase extends RoomDatabase {
    /**
     * singleton instance for database
     */
    private static GBDatabase instance;

    /**
     * abstract getter for Data accessing object
     */
    public abstract CarParkDao carParkDao();


    /**
     * path to asset of pre-populated data
     */
    static String FILE_DB_NAME = "database/hdb-carpark-information.db";

    /**
     * Getter method for Gym Buddy database
     * if the room database is not existed, it creates from database file in assets folder
     */
    public static synchronized GBDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    GBDatabase.class,
                    "db_gym_buddies")
                    .fallbackToDestructiveMigration()
                    .createFromAsset(FILE_DB_NAME)
                    .build();
        }
        return instance;
    }
}
