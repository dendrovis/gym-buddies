package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct;


import java.util.ArrayList;
import java.util.List;


/**
 * FavBuddyRecord is data structure to store user's favoured buddies as list
 * used with FirebaseFirestore to keep track favoured record of user
 *
 *
 * @author Chia Yu
 * @since 2019-10-05
 */
public class FavBuddyRecord {
    /**
     * List of user id, these ids will be used to denoted favoured buddies in the application
     */
    private List<String> buddiesId = new ArrayList<String>();

    /**
     * default constructor
     */
    public FavBuddyRecord() {
    }


    /**
     * alternate constructor
     * @param buddyList
     */
    public FavBuddyRecord(List<String> buddyList) {
        this.buddiesId = buddyList;
    }

    /**
     * getter method for {@link #buddiesId}
     */
    public List getBuddiesId() {
        return buddiesId;
    }

    /**
     * setter method for {@link #buddiesId}
     * @param buddyList
     */
    public void setBuddiesId(List<String> buddyList) {
        this.buddiesId = buddyList;
    }
}


