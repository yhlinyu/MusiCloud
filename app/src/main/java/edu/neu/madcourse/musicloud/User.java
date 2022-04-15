package edu.neu.madcourse.musicloud;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    protected String username;
    protected String password;
    protected String profileImage;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.profileImage = "placeholder";
    }

    public User(String username, String password, String profileImage) {
        this.username = username;
        this.password = password;
        this.profileImage = profileImage;
    }

    public User() {};

    protected User(Parcel in) {
        username = in.readString();
        password = in.readString();
        profileImage = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(profileImage);
    }
}
