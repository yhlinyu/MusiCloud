package edu.neu.madcourse.musicloud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import edu.neu.madcourse.musicloud.comments.Comment;
import edu.neu.madcourse.musicloud.comments.RecyclerViewAdapter;
import edu.neu.madcourse.musicloud.spotify.WebServiceExecutor;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "PostActivity";
    private String token;

    // Post-specific
    private User currUser;
    private String postId;

    // Database
    private DatabaseReference dbReference;
    private DatabaseReference userDbReference;
    private DatabaseReference postDbReference;
    private DatabaseReference commentsDbReference;
    private ValueEventListener postValueEventListener;
    private ChildEventListener commentsChildEventListener;
    private ValueEventListener initPostValueEventListener;

    // Recycler view
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager rViewLayoutManager;
    private ArrayList<Comment> commentsList;

    // Views
    private RelativeLayout navBarLayout;
    private ImageView navBarUserAvatar;
    private ImageView postUserImage;
    private TextView postUsername;
    private TextView postTime;
    private Button followButton;
    private ImageView songImage;
    private TextView songTitle;
    private TextView songArtist;
    private TextView postTitle;
    private TextView postContent;
    private TextView postLikes;
    private TextView postReplies;
    private TextView postShares;
    private TextView commentSectionCnt;
    private Button playButton, pauseButton;
    private TextInputLayout commentInputLayout;
    private TextInputEditText commentInput;
    private MediaPlayer mediaPlayer;

    private PrettyTime p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        p = new PrettyTime();

        // Set the post Id
        postId = "-Mzxvh57q8aioVtBv8VZ";

        // Retrieve and set the current user
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getParcelable("currentUser") != null) {
            currUser = (User) extras.getParcelable("currentUser");
        }

        // For testing
        // currUser = new User("Anon", "password");

        // Initialize empty comments list and create RecyclerView
        commentsList = new ArrayList<>();
        createRecyclerView();

        // Bind views and set on click listeners
        navBarLayout = (RelativeLayout) findViewById(R.id.navbar);
        navBarUserAvatar = navBarLayout.findViewById(R.id.navUserAvatar);
        postUserImage = findViewById(R.id.postUserImg);
        postUsername = findViewById(R.id.postUsername);
        postTime = findViewById(R.id.postTimestamp);
        followButton = findViewById(R.id.followButton);
        songImage = findViewById(R.id.songImg);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        postTitle = findViewById(R.id.postTitle);
        postContent = findViewById(R.id.postContent);
        postLikes = findViewById(R.id.postLikes);
        postReplies = findViewById(R.id.postReplies);
        postShares = findViewById(R.id.postShares);
        commentSectionCnt = findViewById(R.id.commentsCnt);

        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        commentInputLayout = findViewById(R.id.commentsInputLayout);
        commentInput = findViewById(R.id.commentsInput);

        navBarUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayer.isPlaying()) {
                    playButton.setVisibility(View.GONE);
                    pauseButton.setVisibility(View.VISIBLE);
                    mediaPlayer.start();
                } else {
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.GONE);
                    mediaPlayer.pause();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayer.isPlaying()) {
                    playButton.setVisibility(View.GONE);
                    pauseButton.setVisibility(View.VISIBLE);
                    mediaPlayer.start();
                } else {
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.GONE);
                    mediaPlayer.pause();
                }
            }
        });

        commentInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();

            }
        });

        postLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                like();
            }
        });

        // Set up Media Player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        // Set up database
        dbReference = FirebaseDatabase.getInstance().getReference(); // points to the root db
        postDbReference = dbReference.child("posts").child(postId); // points to the post
        commentsDbReference = postDbReference.child("comments"); // points to the comments of the post
        userDbReference = dbReference.child("users").child(currUser.getUsername()); // points to the current user

        // Listen to changes to this post from DB
        postValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postUsername.setText(snapshot.child("user").getValue(User.class).getUsername());
                postTime.setText(p.format(snapshot.child("date").getValue(Date.class)));
                postTitle.setText(snapshot.child("title").getValue(String.class));
                postContent.setText(snapshot.child("content").getValue(String.class));
                postLikes.setText(Integer.toString(snapshot.child("likeCnt").getValue(Integer.class)));

                // Set the like (heart) icon appearance based on the current user
                // If the user has liked this post, the icon should be red
                // Otherwise, the icon should be black
                if (snapshot.child("likes").hasChild(currUser.getUsername())) {
                    Log.v("like", "liked");
                    postLikes.getCompoundDrawables()[0].setTint(Color.RED);
                } else {
                    Log.v("Like", "not liked");
                    postLikes.getCompoundDrawables()[0].setTint(Color.BLACK);
                }

                postReplies.setText(Integer.toString(snapshot.child("commentCnt").getValue(Integer.class)));
                postShares.setText(Integer.toString(snapshot.child("shareCnt").getValue(Integer.class)));
                commentSectionCnt.setText("(" + Integer.toString(snapshot.child("commentCnt").getValue(Integer.class)) + ")");
//                songTitle.setText(snapshot.child("song").child("title").getValue(String.class));
//                songArtist.setText(snapshot.child("song").child("artist").getValue(String.class));

                // Load images from url
                Glide.with(getApplicationContext()).load(snapshot.child("user").
                        getValue(User.class).getProfileImage()).into(postUserImage);
//                Glide.with(getApplicationContext()).load(snapshot.child("song").
//                        getValue(Song.class).getImg()).into(songImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        postDbReference.addValueEventListener(postValueEventListener);

        // Listen to changes to the comments of this post from DB
        commentsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment newComment = snapshot.getValue(Comment.class);
                commentsList.add(newComment);
                Collections.sort(commentsList, Collections.reverseOrder());
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        commentsDbReference.addChildEventListener(commentsChildEventListener);

        // Fetch song
        WebServiceExecutor webServiceExecutor = new WebServiceExecutor();
        webServiceExecutor.execute(new PostActivity.FetchTrackTask());

        // For searching
        // webServiceExecutor.execute(new SpotifyService());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        commentsDbReference.removeEventListener(commentsChildEventListener);
        postDbReference.removeEventListener(postValueEventListener);
    }

    // Clear the text input field focus when user touch elsewhere
    // Code reference: https://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    /**
     * Set up RecyclerView for comments, located at the bottom of the Topic Screen.
     */
    private void createRecyclerView() {
        rViewLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.commentsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(rViewLayoutManager);

        recyclerViewAdapter = new RecyclerViewAdapter(commentsList);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

//    private void initPost() {
//        // Single value event listener to see if post exists
//        // If exists, do nothing
//        // If not, init the post
//
//        initPostValueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (!snapshot.hasChild("title")) {
//                    Intent intent = new Intent();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        };
//
//        postDbReference.addListenerForSingleValueEvent(initPostValueEventListener);
//
//    }

    private void postComment() {
        String content = commentInput.getText().toString();
        Date now = new Date();

        Comment comment = new Comment(currUser, content, now, postId);

        // Add comment to posts (/posts/postId/comments) and to user (/users/userId/comments)
        String commentId = commentsDbReference.push().getKey();
        commentsDbReference.child(commentId).setValue(comment);
        userDbReference.child("comments").child(commentId).setValue(comment);

        // Remove previous comment input
        commentInput.setText("");

        // Update comment count of this post
        postDbReference.child("commentCnt").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                int currCommentCnt = currentData.getValue(Integer.class);
                currentData.setValue(currCommentCnt + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        });
    }

    private void like() {
        // Add or remove the user from `likes` based on whether the user has liked this post before
        // Also, add or remove the post from the user's `likes`
        postDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("likes").hasChild(currUser.getUsername())) {
                    // If the current user has not liked this post
                    postDbReference.child("likes").child(currUser.getUsername()).setValue(currUser);
                    userDbReference.child("likes").child(postId).setValue(true);
                } else {
                    // If the current user has already liked this post and want to dislike
                    postDbReference.child("likes").child(currUser.getUsername()).removeValue();
                    userDbReference.child("likes").child(postId).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Update the like count of this post
        // And update the appearance of the like icon
        postDbReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                int currLikeCnt = currentData.child("likeCnt").getValue(Integer.class);

                // If the user had already liked this post
                if (currentData.child("likes").child(currUser.getUsername()).getValue() != null) {
                    // Set the heart icon to black
                    postLikes.getCompoundDrawables()[0].setTint(Color.BLACK);
                    // Decrement the like cnt
                    currentData.child("likeCnt").setValue(currLikeCnt - 1);

                } else {
                    // Set the heart icon to red
                    postLikes.getCompoundDrawables()[0].setTint(Color.RED);
                    // Increment the like cnt
                    currentData.child("likeCnt").setValue(currLikeCnt + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

            }
        });
    }


    private class FetchTrackTask implements Runnable {
        private static final String client_id = "c443c313a6f64ef4a485998303b4e530";
        private static final String client_secret = "6b487187b36148a1aa5445507653a2f8";
        private static final String authUrlStr = "https://accounts.spotify.com/api/token?grant_type=client_credentials";
        private static final String getTrackUrlStr = "https://api.spotify.com/v1/tracks/";

        @Override
        public void run() {
            URL url = null;
            HttpsURLConnection connection = null;

            // GET TOKEN
            try {
                // Authenticate and request token
                url = new URL(authUrlStr);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                connection.setDoInput(true);

                // Headers of the request
                String credentials = client_id + ":" + client_secret;
                String encodedAuthStr = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

                connection.setRequestProperty("Authorization", encodedAuthStr);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                connection.connect();

                // Read from connection
                InputStream in = connection.getInputStream();
                String response = convertStreamToString(in);

                JSONObject resultsJSON = new JSONObject(response);
                Log.v(TAG, resultsJSON.toString());

                token = resultsJSON.getString("access_token");
                Log.v(TAG, token);

            } catch (Exception e) {
                Log.v(TAG, e.getMessage());
            } finally {
                connection.disconnect();
            }

            // If failed to fetch token, return
            // TODO: add error message
            if (token == null) {
                return;
            }

            // FETCH TRACK with the retrieved token
            try {
                // Fetch track using track id
                String trackId = "60nZcImufyMA1MKQY3dcCH";
                String urlStr = getTrackUrlStr + trackId + "?market=US";

                url = new URL(urlStr);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                connection.setDoInput(true);

                // Headers of the request
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Accept", "application/json");

                connection.connect();

                // Read from connection
                InputStream in = connection.getInputStream();
                String response = convertStreamToString(in);

                JSONObject resultsJSON = new JSONObject(response);
                Log.v(TAG, resultsJSON.toString());
                Log.v(TAG, resultsJSON.getString("preview_url"));

                // Prepare Media Player
                mediaPlayer.setDataSource(resultsJSON.getString("preview_url"));
                mediaPlayer.prepare();

                // Update UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayTrackUI(resultsJSON);
                    }
                });

            } catch (Exception e) {
                Log.v(TAG, e.toString());
            } finally {
                connection.disconnect();
            }
        }
    }

    /**
     * Given the JSON object of a track, set the song title, artist name, and album image
     * in the Post Screen.
     *
     * @param jsonObject the track object retrieved from Spotify Web API
     */
    private void displayTrackUI(JSONObject jsonObject)  {
        try {
            songTitle.setText(trimToFit(jsonObject.getString("name"), 15));
            songArtist.setText(jsonObject.getJSONArray("artists")
                    .getJSONObject(0).getString("name"));
            Glide.with(getApplicationContext())
                    .load(jsonObject.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"))
                    .into(songImage);

        } catch (Exception e) {
            Log.v("displayTrackUI", e.getMessage());
        }
    }


    // ===== HELPER FUNCTIONS =====

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

    private String trimToFit(String str, int limit) {
        String returnStr = "";
        if (str.length() >= limit) {
            returnStr = str.substring(0, limit) + "...";
        } else {
            returnStr = str;
        }
        return returnStr;
    }

    /**
     * Initialize information for testing purposes.
     * Kept for reference.
     */
    private void initTest() {
        PrettyTime p = new PrettyTime();
        String passwordPlaceholder = "placeholder";

        // String urls for images that are not yet stored in server
        String imageStr1 = "https://post.medicalnewstoday.com/wp-content/uploads/sites/3/2020/02/322868_1100-800x825.jpg";
        String imageStr2 = "https://bleedingcool.com/wp-content/uploads/2021/06/Pikachu-color-model-publicity-cel-1200x628.jpg";
        String imageStr3 = "https://cdn.myanimelist.net/images/userimages/9196770.jpg?t=1635597600";
        String imageStr4 = "https://toppng.com/uploads/preview/kuromi-sanrio-kuromi-115631993737djkw53fsh.png";
        String imageStr5 = "https://www.vhv.rs/dpng/d/594-5949124_cinnamoroll-sanrio-hellokitty-bunny-cute-soft-cinnamoroll-sanrio.png";

        // Dummy users that posted comments
        User user1 = new User("Dog", passwordPlaceholder, imageStr1);
        User user2 = new User("Pikachu", passwordPlaceholder, imageStr2);
        User user3 = new User("Egg", passwordPlaceholder, imageStr3);
        User user4 = new User("Kuromi", passwordPlaceholder, imageStr4);
        User user5 = new User("Cinnamoroll", passwordPlaceholder, imageStr5);

        // Dummy date objects
        Date tenMinutesAgo = new Date(System.currentTimeMillis() - 1000*60*10);
        Date oneDayAgo = new Date(System.currentTimeMillis() - 1000*60*60*24);
        Date twoDaysAgo = new Date(System.currentTimeMillis() - 1000*60*60*48);
        Date now = new Date();

        // ====== CREATE DUMMY COMMENTS ======
        Comment comment1 = new Comment(user1, "Thank you for the rec!", now, postId, 10);
        Comment comment2 = new Comment(user2, "Wow!!", tenMinutesAgo, postId, 5);
        Comment comment3 = new Comment(user3, "I like this song", oneDayAgo, postId, 1);
        Comment comment4 = new Comment(user4, "I like your music taste!", oneDayAgo, postId);
        Comment comment5 = new Comment(user5, "Thanks for recommending!", twoDaysAgo, postId);

        // Push the comments to the DB
        commentsDbReference.push().setValue(comment1);
        commentsDbReference.push().setValue(comment2);
        commentsDbReference.push().setValue(comment3);
        commentsDbReference.push().setValue(comment4);
        commentsDbReference.push().setValue(comment5);

        // Retrieve post information from DB using postId
        // String postId = "-Mzxvh57q8aioVtBv8VZ"; // sunroof post

        postValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postUsername.setText(snapshot.child("user").getValue(User.class).getUsername());
                postTime.setText(p.format(snapshot.child("date").getValue(Date.class)));
                postTitle.setText(snapshot.child("title").getValue(String.class));
                postContent.setText(snapshot.child("content").getValue(String.class));
                postLikes.setText(Integer.toString(snapshot.child("likeCnt").getValue(Integer.class)));
                postReplies.setText(Integer.toString(snapshot.child("commentCnt").getValue(Integer.class)));
                postShares.setText(Integer.toString(snapshot.child("shareCnt").getValue(Integer.class)));
                commentSectionCnt.setText("(" + Integer.toString(snapshot.child("commentCnt").getValue(Integer.class)) + ")");
                songTitle.setText(snapshot.child("song").child("title").getValue(String.class));
                songArtist.setText(snapshot.child("song").child("artist").getValue(String.class));

                // Load images from url
                Glide.with(getApplicationContext()).load(snapshot.child("user").
                        getValue(User.class).getProfileImage()).into(postUserImage);
                Glide.with(getApplicationContext()).load(snapshot.child("song").
                        getValue(Song.class).getImg()).into(songImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        postDbReference.addValueEventListener(postValueEventListener);
    }
}