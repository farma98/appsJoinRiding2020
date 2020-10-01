package com.example.joinriding.models;

public class ModelPosts {
    private String idPosting, namePosting, photoPosting, descriptionPosting, timePosting, likePosting, commentPosting;
    private String uid, uEmail, uPhoto, uName;

    public ModelPosts() {
    }

    public ModelPosts(String idPosting, String namePosting, String photoPosting, String descriptionPosting, String timePosting, String likePosting, String commentPosting, String uid, String uEmail, String uPhoto, String uName) {
        this.idPosting = idPosting;
        this.namePosting = namePosting;
        this.photoPosting = photoPosting;
        this.descriptionPosting = descriptionPosting;
        this.timePosting = timePosting;
        this.likePosting = likePosting;
        this.commentPosting = commentPosting;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uPhoto = uPhoto;
        this.uName = uName;
    }

    public String getIdPosting() {
        return idPosting;
    }

    public void setIdPosting(String idPosting) {
        this.idPosting = idPosting;
    }

    public String getNamePosting() {
        return namePosting;
    }

    public void setNamePosting(String namePosting) {
        this.namePosting = namePosting;
    }

    public String getPhotoPosting() {
        return photoPosting;
    }

    public void setPhotoPosting(String photoPosting) {
        this.photoPosting = photoPosting;
    }

    public String getDescriptionPosting() {
        return descriptionPosting;
    }

    public void setDescriptionPosting(String descriptionPosting) {
        this.descriptionPosting = descriptionPosting;
    }

    public String getTimePosting() {
        return timePosting;
    }

    public void setTimePosting(String timePosting) {
        this.timePosting = timePosting;
    }

    public String getLikePosting() {
        return likePosting;
    }

    public void setLikePosting(String likePosting) {
        this.likePosting = likePosting;
    }

    public String getCommentPosting() {
        return commentPosting;
    }

    public void setCommentPosting(String commentPosting) {
        this.commentPosting = commentPosting;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuPhoto() {
        return uPhoto;
    }

    public void setuPhoto(String uPhoto) {
        this.uPhoto = uPhoto;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}