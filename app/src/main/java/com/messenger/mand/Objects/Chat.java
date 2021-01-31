package com.messenger.mand.Objects;

public class Chat {
    private String initiator;
    private String recipient;

    public Chat() {}

    public Chat(String initiator, String recipient) {
        this.initiator = initiator;
        this.recipient = recipient;
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
}