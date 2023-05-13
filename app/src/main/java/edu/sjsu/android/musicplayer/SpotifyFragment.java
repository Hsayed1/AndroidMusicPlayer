package edu.sjsu.android.musicplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import edu.sjsu.android.musicplayer.databinding.FragmentSpotifyBinding;
import edu.sjsu.android.musicplayer.databinding.FragmentTabbedBinding;

public class SpotifyFragment extends Fragment {
    private FragmentSpotifyBinding binding;
    private static final String CLIENT_ID = "2bbb51fe700f4a40804482e9ed90fc05";
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "http://edu.sjsu.android.musicplayer/callback";
    private static final String SPOTIFY_BASE_URL = "https://api.spotify.com/v1/search?";
    private String accessToken = null;
    List<Track> searchResults = new ArrayList<>();

    public static SpotifyFragment newInstance() {
        SpotifyFragment fragment = new SpotifyFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentSpotifyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.noSpotifySongs.setVisibility(View.VISIBLE);

        binding.spotifyList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.spotifyList.setAdapter(new SpotifyAdapter(searchResults, getContext()));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming, app-remote-control"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(getActivity(), REQUEST_CODE, request);

    }

    public void handleSpotifyAuthorization(int requestCode, int resultCode, Intent intent) {
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    accessToken = response.getAccessToken();
                    Log.i("SpotAuth", "Token is " + accessToken);
                    break;
                case ERROR:
                    Log.i("SpotAuth", response.getError());
                    break;
                default:
                    Log.i("SpotAuth", "default");
                    return;
            }
        }
    }

    class HttpTask extends AsyncTask<String, Void, List<Track>> {
        @Override
        protected List<Track> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                urlConnection.disconnect();
                System.out.println(content);
                JSONObject response = new JSONObject(content.toString());
                JSONObject tracks = response.getJSONObject("tracks");
                JSONArray items = tracks.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    String title = items.getJSONObject(i).getString("name");
                    String uri = items.getJSONObject(i).getString("uri");
                    JSONArray images = items.getJSONObject(i).getJSONObject("album").getJSONArray("images");
                    String image = images.getJSONObject(images.length() - 1).getString("url");
                    searchResults.add(new Track(title, uri, image));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return searchResults;
        }

    @Override
    protected void onPostExecute(List<Track> searchResults) {
        System.out.println("Size of search results: " + searchResults.size());
        binding.spotifyList.setAdapter(new SpotifyAdapter(searchResults, getContext()));
    }
}
    public void handleSearch(String searchText) {
        if(searchText.isEmpty()){
            binding.noSpotifySongs.setVisibility(View.VISIBLE);
            return;
        }
        searchResults.clear();
        new HttpTask().execute(SPOTIFY_BASE_URL + "q=" + StringEscapeUtils.escapeHtml4(searchText) + "&type=track" + "&access_token=" + accessToken);

    }
}