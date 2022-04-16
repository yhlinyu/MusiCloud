package edu.neu.madcourse.musicloud;

import java.util.Date;

public class Post {
    protected User user;
    protected String title;
    protected String content;
    protected Date date;
    protected Song song;
    protected int likeCnt;
    protected int commentCnt;
    protected int shareCnt;

    /**
     * Constructs a new Post object and initialize the counts for likes, comments,
     * and shares to 0.
     *
     * @param user
     * @param title
     * @param content
     * @param date
     */
    public Post(User user, String title, String content, Song song, Date date) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.date = date;
        this.song = song;
        this.likeCnt = 0;
        this.commentCnt = 0;
        this.shareCnt = 0;
    }

    /**
     * Constructs a Post object with the given params.
     * This should be used if the post already exists.
     *
     * @param user
     * @param title
     * @param content
     * @param date
     * @param likeCnt
     * @param commentCnt
     * @param shareCnt
     */
    public Post(User user, String title, String content, Date date, Song song,
                int likeCnt, int commentCnt, int shareCnt) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.date = date;
        this.song = song;
        this.likeCnt = likeCnt;
        this.commentCnt = commentCnt;
        this.shareCnt = shareCnt;
    }

    public Post() {};

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public int getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(int likeCnt) {
        this.likeCnt = likeCnt;
    }

    public int getCommentCnt() {
        return commentCnt;
    }

    public void setCommentCnt(int commentCnt) {
        this.commentCnt = commentCnt;
    }

    public int getShareCnt() {
        return shareCnt;
    }

    public void setShareCnt(int shareCnt) {
        this.shareCnt = shareCnt;
    }
}
