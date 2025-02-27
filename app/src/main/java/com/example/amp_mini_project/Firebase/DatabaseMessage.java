package com.example.amp_mini_project.Firebase;

public class DatabaseMessage {

    private String senderId;
    private String receiverId;
    private String itemId;
    private String text;
    private long timestamp;
    private boolean read;
    private boolean sendPhoneNumber;
    private boolean sendEmail;

    public DatabaseMessage() {
    }

    public DatabaseMessage(String senderId, String receiverId, String itemId, String text, long timestamp, boolean read, boolean sendPhoneNumber, boolean sendEmail) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.itemId = itemId;
        this.text = text;
        this.timestamp = timestamp;
        this.read = read;
        this.sendPhoneNumber = sendPhoneNumber;
        this.sendEmail = sendEmail;
    }

    public String getSenderId() {
        return senderId;
    }
    public String getReceiverId() {
        return receiverId;
    }
    public String getItemId() {
        return itemId;
    }

    public String getText() {
        return text;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public boolean isRead() {
        return read;
    }
    public boolean isSendPhoneNumber() {
        return sendPhoneNumber;
    }
    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
    public void setSendPhoneNumber(boolean sendPhoneNumber) {
        this.sendPhoneNumber = sendPhoneNumber;
    }
    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getFormattedTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }

    public boolean isUnreadFor(String currentUserId) {
        return !read && receiverId.equals(currentUserId);
    }

    public String getOtherPersonId(String currentUserId) {
        if (senderId.equals(currentUserId)) {
            return receiverId;
        } else {
            return senderId;
        }
    }
}
