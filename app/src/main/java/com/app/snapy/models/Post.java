package com.app.snapy.models;

import java.io.Serializable;

public class Post implements Serializable {
    private String postId;
    private String postUrl;
    private String postCaption;
    private String postRating;
    private String userID;
    private String status;
    private String publishDateTime;
    public String getPublishDateTime() {
        return publishDateTime;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setPublishDateTime(String publishDateTime) {
        this.publishDateTime = publishDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getPostCaption() {
        return postCaption;
    }

    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }

    public String getPostRating() {
        return postRating;
    }

    public void setPostRating(String postRating) {
        this.postRating = postRating;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
