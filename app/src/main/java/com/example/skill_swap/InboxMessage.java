package com.example.skill_swap;

import com.google.firebase.Timestamp;

import java.util.Date;

public class InboxMessage {

    private String toUserId;
    private String fromUserId;
    private String senderUid;
    private String text;
    private boolean read;
    private Object timestamp; // Can be Timestamp or Long

    public InboxMessage() {
        // Required empty constructor for Firestore deserialization
    }

    public String getToUserId() { return toUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getSenderUid() { return senderUid; }
    public void setSenderUid(String senderUid) { this.senderUid = senderUid; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    // ✅ Safe helper to convert to Date regardless of Firestore data type
    public Date getTimestampAsDate() {
        if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).toDate();
        } else if (timestamp instanceof Long) {
            return new Date((Long) timestamp); // Firestore sometimes stores as millis
        }
        return null;
    }

}
