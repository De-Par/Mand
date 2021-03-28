package com.messenger.mand.entities;

public class User {

    private String name;
    private String email;
    private String id;
    private String avatar;
    private String status;
    private String dateCreation;
    private String searchName;
    private String phone;
    private String dateBirth;
    private String sex;
    private String aboutMe;;

    public User() {}

    public User(String name, String email, String id, String avatar, String status,
                String dateCreation, String searchName, String phone,
                String dateBirth, String sex, String aboutMe) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.avatar = avatar;
        this.status = status;
        this.dateCreation = dateCreation;
        this.searchName = searchName;
        this.phone = phone;
        this.dateBirth = dateBirth;
        this.sex = sex;
        this.aboutMe = aboutMe;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateBirth() {
        return dateBirth;
    }

    public void setDateBirth(String dateBirth) {
        this.dateBirth = dateBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }
}