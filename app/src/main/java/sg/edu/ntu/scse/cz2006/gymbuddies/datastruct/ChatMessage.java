package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct;


/**
 *  Created by Chia Yu on 22/10/2019.
 *  for sg.edu.ntu.scse.cz2006.gymbuddies.datastruct in Gym Buddies!
 *
 *  Entity class to hold single chat message between user.
 *
 * @author Chia Yu
 * @since 2019-10-22
 */
public class ChatMessage {
    /**
     * the message content. It is nullable to denote that message is being deleted
     */
    private String message;
    /**
     * user id of user that send out the message
     */
    private String sender;
    /**
     * timestamp of message of user send out the message
     */
    private long timestamp;

    /**
     * default constructor
     */
    public ChatMessage(){}

    /** alternate constructor
     *
     * @param message
     * @param sender
     * @param timestamp
     */
    public ChatMessage(String message, String sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    /**
     * alternate constructor
     * @param message
     * @param sender
     */
    public ChatMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }


    /**
     * getter method of {@link #message}
     */
    public String getMessage() {
        return message;
    }

    /**
     * setter method of {@link #message}
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * getter method of {@link #sender}
     */
    public String getSender() {
        return sender;
    }

    /**
     * setter method of {@link #sender}
     * @param sender
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * getter method of {@link #timestamp}
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * setter method of {@link #timestamp}
     * @param timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
