package edu.neu.madcourse.musicloud.comments;

import java.util.Date;

import edu.neu.madcourse.musicloud.Post;
import edu.neu.madcourse.musicloud.Song;
import edu.neu.madcourse.musicloud.User;

public class Comment implements Comparable<Comment> {
    protected User user;
    protected String content;
    protected  Date date;
    protected int likeCnt;


    public Comment(User user, String content, Date date) {
        this.user = user;
        this.content = content;
        this.date = date;
        this.likeCnt = 0;

    }

    public Comment(User user, String content, Date date, int likeCnt) {
        this.user = user;
        this.content = content;
        this.date = date;
        this.likeCnt = likeCnt;
    }

    public Comment() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(int likeCnt) {
        this.likeCnt = likeCnt;
    }

    @Override
    public int compareTo(Comment comment) {
        return getDate().compareTo(comment.getDate());
    }
}
