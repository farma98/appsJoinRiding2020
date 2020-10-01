package com.example.joinriding.models;

public class ModelMyListUsers {
    private String uid, nameUser, genderUser, addressUser, phoneUser, simUser, photoUser, emailUser, passwordUser, searchUser, coverUser, onlineStatus, typingTo;
    private boolean isBlocked = false;
    private boolean isFollow = false;

    public ModelMyListUsers() {
    }

    public ModelMyListUsers(String uid, String nameUser, String genderUser, String addressUser, String phoneUser, String simUser, String photoUser, String emailUser, String passwordUser, String searchUser, String coverUser, String onlineStatus, String typingTo, boolean isBlocked, boolean isFollow) {
        this.uid = uid;
        this.nameUser = nameUser;
        this.genderUser = genderUser;
        this.addressUser = addressUser;
        this.phoneUser = phoneUser;
        this.simUser = simUser;
        this.photoUser = photoUser;
        this.emailUser = emailUser;
        this.passwordUser = passwordUser;
        this.searchUser = searchUser;
        this.coverUser = coverUser;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.isBlocked = isBlocked;
        this.isFollow = isFollow;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getGenderUser() {
        return genderUser;
    }

    public void setGenderUser(String genderUser) {
        this.genderUser = genderUser;
    }

    public String getAddressUser() {
        return addressUser;
    }

    public void setAddressUser(String addressUser) {
        this.addressUser = addressUser;
    }

    public String getPhoneUser() {
        return phoneUser;
    }

    public void setPhoneUser(String phoneUser) {
        this.phoneUser = phoneUser;
    }

    public String getSimUser() {
        return simUser;
    }

    public void setSimUser(String simUser) {
        this.simUser = simUser;
    }

    public String getPhotoUser() {
        return photoUser;
    }

    public void setPhotoUser(String photoUser) {
        this.photoUser = photoUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getPasswordUser() {
        return passwordUser;
    }

    public void setPasswordUser(String passwordUser) {
        this.passwordUser = passwordUser;
    }

    public String getSearchUser() {
        return searchUser;
    }

    public void setSearchUser(String searchUser) {
        this.searchUser = searchUser;
    }

    public String getCoverUser() {
        return coverUser;
    }

    public void setCoverUser(String coverUser) {
        this.coverUser = coverUser;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public boolean isFollow() {
        return isFollow;
    }

    public void setFollow(boolean follow) {
        isFollow = follow;
    }
}
