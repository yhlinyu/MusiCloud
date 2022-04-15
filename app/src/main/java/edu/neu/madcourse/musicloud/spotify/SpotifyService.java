package edu.neu.madcourse.musicloud.spotify;

import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class SpotifyService implements Runnable {
    private static final String TAG = "SpotifyApi";
    private static final String client_id = "c443c313a6f64ef4a485998303b4e530";
    private static final String client_secret = "6b487187b36148a1aa5445507653a2f8";
    private static final String authUrlStr = "https://accounts.spotify.com/api/token?grant_type=client_credentials";
    private static final String getTrackUrlStr = "https://api.spotify.com/v1/tracks/";
    private static final String searchTrackUrlStr = "https://api.spotify.com/v1/search";

    private String token;

    @Override
    public void run() {
        URL url = null;
        HttpsURLConnection connection = null;

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

            // Reads from connection
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
        if (token == null) {
            return;
        }

        try {
            // Fetch track using id
            String queryStr = "?q=sunroof&type=track&limit=30";
            String urlStr = searchTrackUrlStr + queryStr;

            url = new URL(urlStr);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setDoInput(true);

            // Headers of the request
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("Accept", "application/json");

            connection.connect();

            // Reads from connection
            InputStream in = connection.getInputStream();
            String response = convertStreamToString(in);

            JSONObject resultsJSON = new JSONObject(response);
            Log.v(TAG, resultsJSON.toString());
            Log.v(TAG, resultsJSON.getJSONObject("tracks").getJSONArray("items")
            .getJSONObject(0).getString("id"));


        } catch (Exception e) {
            Log.v(TAG, e.toString());
        } finally {
            connection.disconnect();
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

}
