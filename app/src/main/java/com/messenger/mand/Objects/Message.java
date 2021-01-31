package com.messenger.mand.Objects;

public class Message {

    private String text;
    private String time;
    private String sender;
    private String recipient;
    private String imageUrl;
    private boolean isMineMessage;
    private String isSeen;

    public Message() {}

    public Message(String text, String time, String sender, String recipient,
                   String imageUrl, boolean isMineMessage, String isSeen) {
        this.text = text;
        this.time = time;
        this.sender = sender;
        this.recipient = recipient;
        this.imageUrl = imageUrl;
        this.isMineMessage = isMineMessage;
        this.isSeen = isSeen;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isMineMessage() { return isMineMessage; }

    public void setMineMessage(boolean mineMessage) {
        this.isMineMessage = mineMessage;
    }

    public String getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(String isSeen) {
        this.isSeen = isSeen;
    }
}
