package sg.edu.ntu.scse.cz2006.gymbuddies;


/**
 * Interface to provides application related constants
 * For sg.edu.ntu.scse.cz2006.gymbuddies in Gym Buddies!
 *
 * @author Chia Yu
 * @since 2019-10-22
 */
public interface AppConstants {
    /**
     * Firestore database collection key for favbuddy
     */
    String COLLECTION_FAV_BUDDY = "favbuddy";
    /**
     * Firestore database collection key for chat
     */
    String COLLECTION_CHAT = "chat";
    /**
     * Firestore database collection key for message
     */
    String COLLECTION_MESSAGES = "message";


    /**
     * Valid max duration for image cache
     * @see sg.edu.ntu.scse.cz2006.gymbuddies.util.DiskIOHelper
     */
    long MAX_CACHE_DURATION = 1000*60*60*24; // 1 day
}
