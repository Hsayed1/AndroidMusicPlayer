package edu.sjsu.android.musicplayer;

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

import edu.sjsu.android.musicplayer.databinding.FragmentTabbedBinding;

public class SpotifyFragment extends Fragment {
    private FragmentTabbedBinding binding;
    private static final String CLIENT_ID = "2bbb51fe700f4a40804482e9ed90fc05";
    private static final String REDIRECT_URI = "http://edu.sjsu.android.musicplayer/callback";
    private SpotifyAppRemote mSpotifyAppRemote;

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

        binding = FragmentTabbedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.sectionLabel;
        textView.setText("Fragment 1");
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
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        // Connect to app remote
        SpotifyAppRemote.connect(getContext(), connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("LocalMusicFragment", "Connected! Yay!");
                        // Start interacting with app remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("LocalMusicFragment", throwable.getMessage(), throwable);
                        // Connection failed, handle errors here
                    }
                });
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

    @Override
    public void onStop() {
        super.onStop();
        // Disconnect app remote
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}