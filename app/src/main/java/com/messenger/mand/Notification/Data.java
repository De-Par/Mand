package com.messenger.mand.Notification;

public class Data {
    private String user;
    private String title;
    private String sender;
    private String body;
    private int icon;

    public Data() {}

    public Data( String sender, String user, String title, String body, int icon) {
        this.user = user;
        this.title = title;
        this.sender = sender;
        this.body = body;
        this.icon = icon;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
