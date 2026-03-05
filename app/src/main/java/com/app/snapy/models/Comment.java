package com.app.snapy.models;

import java.io.Serializable;

public class Comment implements Serializable {

    private String userId;
    public String commentCaption;
    public String commentPublishDate;

    public String getCommentPublishDate() {
        return commentPublishDate;
    }

    public void setCommentPublishDate(String commentPublishDate) {
        this.commentPublishDate = commentPublishDate;
    }

    public String getCommentCaption() {
        return commentCaption;
    }

    public void setCommentCaption(String commentCaption) {
        this.commentCaption = commentCaption;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
