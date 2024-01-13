package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct;

import com.google.firebase.firestore.Exclude;

import java.util.HashMap;


/**
 * Data Structure to store the Chat data in FirebaseFirestore.
 * For sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 *
 * @author Chia Yu
 * @since 2019-10-22
 */
public class Chat {
    private String lastMessage="";
    private long lastUpdate=0;
    private HashMap<String, Boolean> participant = new HashMap<>();

    String chatId;
    User otherUser;

    /**
     * default constructor
     */
    public Chat(){}

    /**
     * alternate constructor
     */
    public Chat(String lastMessage, long lastUpdate, HashMap<String, Boolean> participant) {
        this.lastMessage = lastMessage;
        this.lastUpdate = lastUpdate;
        this.participant = participant;
    }

    /**
     * getter method for last message
     * @return
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * setter method for set lastMessage
     * @param lastMessage
     */
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * getter method to get last update timestamp
     * @return
     */
    public long getLastUpdate() {
        return lastUpdate;
    }


    /**
     * setter method to get last update timestamp
     * @param lastUpdate
     */
    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * getter method for member of the chat
     * @return
     */
    public HashMap<String, Boolean> getParticipant() {
        return participant;
    }

    /**
     * setter method member of the chat
     * @param participant
     */
    public void setParticipant(HashMap<String, Boolean> participant) {
        this.participant = participant;
    }

    /**
     * getter method for chat id
     * @return
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * setter method for chat id
     * @param chatId
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
     * excluded to save otherUser attribute in FirebaseFirestore
     * @return user of the other participant
     */
    @Exclude
    public User getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }

    /**
     * for display purpose only
     * @return
     */
    @Override
    public String toString() {
        return "Chat{" +
                "lastMessage='" + lastMessage + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", participant=" + participant +
                ", chatId='" + chatId + '\'' +
                ", otherUser=" + otherUser +
                '}';
    }
}
