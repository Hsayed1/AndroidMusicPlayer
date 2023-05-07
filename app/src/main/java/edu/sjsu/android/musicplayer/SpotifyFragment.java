package edu.sjsu.android.musicplayer;

import android.content.Intent;
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

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import edu.sjsu.android.musicplayer.databinding.FragmentSpotifyBinding;
import edu.sjsu.android.musicplayer.databinding.FragmentTabbedBinding;

public class SpotifyFragment extends Fragment {
    //private FragmentTabbedBinding binding;
    private FragmentSpotifyBinding binding;
    private static final String CLIENT_ID = "f1650e242a8f4adeabc4f3064c95f022";
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "edu.sjsu.android.musicplayer://callback";
    private static final String SPOTIFY_BASE_URL = "https://api.spotify.com/v1";
    private SpotifyAppRemote mSpotifyAppRemote;
    private CronetEngine cronetEngine;
    private Executor executor;
    private String accessToken = null;

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

        CronetEngine.Builder myBuilder = new CronetEngine.Builder(getContext());
        cronetEngine = myBuilder.build();
        executor = Executors.newSingleThreadExecutor();

//        final TextView textView = binding.sectionLabel;
//        textView.setText("Fragment 1");
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

//        //Set the connection parameters
//        ConnectionParams connectionParams =
//                new ConnectionParams.Builder(CLIENT_ID)
//                        .setRedirectUri(REDIRECT_URI)
//                        .showAuthView(true)
//                        .build();
//        // Connect to app remote
//        SpotifyAppRemote.connect(getContext(), connectionParams,
//                new Connector.ConnectionListener() {
//                    @Override
//                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
//                        mSpotifyAppRemote = spotifyAppRemote;
//                        Log.d("SpotifyFragment", "Connected! Yay!");
//                        // Start interacting with app remote
//                        connected();
//                    }
//
//                    @Override
//                    public void onFailure(Throwable throwable) {
//                        Log.e("SpotifyFragment", throwable.getMessage(), throwable);
//                        // Connection failed, handle errors here
//                    }
//                });
    }

    public void handleSpotifyAuthorization(int requestCode, int resultCode, Intent intent) {
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    accessToken = response.getAccessToken();
                    Log.i("haha", "token is " + accessToken);
                    break;
                case ERROR:
                    break;
                default:
                    return;
            }
        }
    }

    private void connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Toast.makeText(getContext(), track.name + " by " + track.artist.name, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void handleSearch(String searchText) {
        if(searchText.isEmpty()){
            return;
        }
        String url = SPOTIFY_BASE_URL + "/q=" + searchText + "&type=track";
        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(url, new MyUrlRequestCallback(), executor);

        UrlRequest request = requestBuilder.build();
        request.start();
        //binding.list.setAdapter(new MusicListAdapter(null, getContext()));
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private class MyUrlRequestCallback extends UrlRequest.Callback {
        private static final String TAG = "MyUrlRequestCallback";

        @Override
        public void onRedirectReceived(UrlRequest request, UrlResponseInfo info, String newLocationUrl) {
        }

        @Override
        public void onResponseStarted(UrlRequest request, UrlResponseInfo info) {
            request.read(ByteBuffer.allocateDirect(102400));
        }

        @Override
        public void onReadCompleted(UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) {
            byteBuffer.clear();
            request.read(byteBuffer);
        }

        @Override
        public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
            Log.i(TAG, "onSucceeded method called.");
        }

        @Override
        public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {

        }
    }
}