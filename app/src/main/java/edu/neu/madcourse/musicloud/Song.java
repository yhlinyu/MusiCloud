package edu.neu.madcourse.musicloud;

public class Song {
    protected String id;
    protected String title;
    protected String artist;
    protected String img;

    public Song(String id, String title, String artist, String img) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.img = img;
    }

    public Song(String id) {
        this.id = id;
    }

    public Song() {};

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
