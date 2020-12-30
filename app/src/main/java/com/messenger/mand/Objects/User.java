package com.messenger.mand.Objects;

public class User {

    private String name;
    private String email;
    private String id;
    private String avatar;
    private String status;
    private String dateCreation;
    private String searchName;
    private String lastMessage;

    public User() {}

    public User(String name, String email, String id, String avatar,
                String status, String dateCreation, String searchName,
                String lastMessage) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.avatar = avatar;
        this.status = status;
        this.dateCreation = dateCreation;
        this.searchName = searchName;
        this.lastMessage = lastMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getLastMessage() { return lastMessage; }

    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

}