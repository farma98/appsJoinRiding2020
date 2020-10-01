package com.example.joinriding.models;

public class ModelComments {
    String commentId, commentMessage, commentTime, uid, uPhoto, uName;

    public ModelComments() {

    }

    public ModelComments(String commentId, String commentMessage, String commentTime, String uid, String uPhoto, String uName) {
        this.commentId = commentId;
        this.commentMessage = commentMessage;
        this.commentTime = commentTime;
        this.uid = uid;
        this.uPhoto = uPhoto;
        this.uName = uName;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentMessage() {
        return commentMessage;
    }

    public void setCommentMessage(String commentMessage) {
        this.commentMessage = commentMessage;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
