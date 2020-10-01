package com.example.joinriding.models;

public class ModelGroupList {
    private String idGroup, nameGroup, descriptionGroup, photoGroup, timeGroup, createdgroup;

    public ModelGroupList() {

    }

    public ModelGroupList(String idGroup, String nameGroup, String descriptionGroup, String photoGroup, String timeGroup, String createdgroup) {
        this.idGroup = idGroup;
        this.nameGroup = nameGroup;
        this.descriptionGroup = descriptionGroup;
        this.photoGroup = photoGroup;
        this.timeGroup = timeGroup;
        this.createdgroup = createdgroup;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getNameGroup() {
        return nameGroup;
    }

    public void setNameGroup(String nameGroup) {
        this.nameGroup = nameGroup;
    }

    public String getDescriptionGroup() {
        return descriptionGroup;
    }

    public void setDescriptionGroup(String descriptionGroup) {
        this.descriptionGroup = descriptionGroup;
    }

    public String getPhotoGroup() {
        return photoGroup;
    }

    public void setPhotoGroup(String photoGroup) {
        this.photoGroup = photoGroup;
    }

    public String getTimeGroup() {
        return timeGroup;
    }

    public void setTimeGroup(String timeGroup) {
        this.timeGroup = timeGroup;
    }

    public String getCreatedgroup() {
        return createdgroup;
    }

    public void setCreatedgroup(String createdgroup) {
        this.createdgroup = createdgroup;
    }
}
