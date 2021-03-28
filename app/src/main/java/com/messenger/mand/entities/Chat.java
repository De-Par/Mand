package com.messenger.mand.entities;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class Chat {
    private String initiator;
    private String recipient;
    private String key;

    public Chat() {}

    public Chat(String initiator, String key, String recipient) {
        this.initiator = initiator;
        this.recipient = recipient;
        this.key = key;
    }

    public String getInitiator() { return initiator; }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @NotNull
    @Override
    public String toString() {
        return "Chat{" +
                "initiator='" + initiator + '\'' +
                ", recipient='" + recipient + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    public void showInTerminal() {
        Log.e("TAG", "Chat{" +
                "initiator='" + initiator + '\'' +
                ", recipient='" + recipient + '\'' +
                ", key='" + key + '\'' +
                '}');
    }
}