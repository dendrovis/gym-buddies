package sg.edu.ntu.scse.cz2006.gymbuddies.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * Data Model for entity 'hdbcarparks' in the Room database.
 * For sg.edu.ntu.scse.cz2006.gymbuddies.data in Gym Buddies!
 *
 * the object represent a row of record on 'hdbcarparks' table in room database
 *
 * @author Chia Yu
 * @since 2019-09-14
 */
@Entity(tableName = "hdbcarparks")
public class CarPark {

    /**
     * primary key of schema, relate to 'car_park_no' in actual table
     */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "car_park_no")
    public String id;

    /**
     * attribute of schema, relate to 'address' in actual table
     */
    @NonNull
    @ColumnInfo(name = "address")
    public String address;

    /**
     * attribute of schema, relate to 'x_coord' in actual table
     */
    @NonNull
    @ColumnInfo(name = "x_coord")
    public double x;

    /**
     * attribute of schema, relate to 'y_coord' in actual table
     */
    @NonNull
    @ColumnInfo(name = "y_coord")
    public double y;

    /**
     * attribute of schema, relate to 'car_park_type' in actual table
     */
    @NonNull
    @ColumnInfo(name = "car_park_type")
    public String carParkType;

    /**
     * attribute of schema, relate to 'type_of_parking_system' in actual table
     */
    @NonNull
    @ColumnInfo(name = "type_of_parking_system")
    public String systemType;

    /**
     * attribute of schema, relate to 'short_term_parking' in actual table
     */
    @NonNull
    @ColumnInfo(name = "short_term_parking")
    public String shortTermParking;

    /**
     * attribute of schema, relate to 'free_parking' in actual table
     */
    @NonNull
    @ColumnInfo(name = "free_parking")
    public String freeParking;

    /**
     * attribute of schema, relate to 'night_parking' in actual table
     */
    @NonNull
    @ColumnInfo(name = "night_parking")
    public String nightParking;

    /**
     * attribute of schema, relate to 'car_park_decks' in actual table
     */
    @NonNull
    @ColumnInfo(name = "car_park_decks")
    public int decks;

    /**
     * attribute of schema, relate to 'gantry_height' in actual table
     */
    @NonNull
    @ColumnInfo(name = "gantry_height")
    public double gantryHeight;

    /**
     * attribute of schema, relate to 'address' in actual table
     */
    @NonNull
    @ColumnInfo(name = "car_park_basement")
    public String basement;

    /**
     * formatted object to human readable text to represent record
     */
    @Override
    public String toString() {
        return "CarPark{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", carParkType='" + carParkType + '\'' +
                ", systemType='" + systemType + '\'' +
                ", shortTermParking='" + shortTermParking + '\'' +
                ", freeParking='" + freeParking + '\'' +
                ", nightParking='" + nightParking + '\'' +
                ", decks=" + decks +
                ", gantryHeight=" + gantryHeight +
                ", basement='" + basement + '\'' +
                '}';
    }
}
