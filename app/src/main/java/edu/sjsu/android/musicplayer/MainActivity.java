package edu.sjsu.android.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "2bbb51fe700f4a40804482e9ed90fc05";
    private static final String REDIRECT_URI = "http://edu.sjsu.android.musicplayer/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    RecyclerView list;
    TextView noSongs;
    List<Audio> filteredSongList = new ArrayList<>();
    List<Audio> originalSongList = new ArrayList<>();
    MaterialToolbar topAppBar;
    LinearLayout searchBar;
    EditText searchBarText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(R.id.list);
        topAppBar = findViewById(R.id.topAppBar);
        searchBar = findViewById(R.id.search_bar);
        searchBarText = findViewById(R.id.search_bar_text);
        noSongs = findViewById(R.id.noSongs);
        noSongs.setVisibility(View.INVISIBLE);

        //setSupportActionBar(topAppBar);

        //checks if permission is granted before getting local music files
        if(!checkPermission()){
            requestPermission();
            return;
        }

        handleMenuSearch();

        String[] projection = {MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION };
        //passing because we only want music from the database
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        while(cursor.moveToNext()){
            //reading database and creating an audio object from the data
            Audio song = new Audio(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if(new File(song.getPath()).exists()) //checking to see if the song exists then adding it to the list
                originalSongList.add(song);
        }

        filteredSongList = new ArrayList<>(originalSongList);
        if(filteredSongList.size() == 0) //if there are no songs show message
            noSongs.setVisibility(View.VISIBLE);
        else {
            //setting the recycler view
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(new MusicListAdapter(filteredSongList, getApplicationContext()));
        }
    }

    private void handleMenuSearch() {
        topAppBar.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getItemId() == R.id.search_songs){
                if(searchBar.getVisibility() != View.VISIBLE){
                    searchBar.setVisibility(View.VISIBLE);
                }
                else {
                    searchBar.setVisibility(View.INVISIBLE);
                }
                return true;
            }
            return false;
        });
        searchBarText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    filteredSongList = originalSongList
                            .stream()
                            .filter(song -> song
                                    .getTitle()
                                    .toLowerCase()
                                    .contains(charSequence)
                            ).collect(Collectors.toList());
                    list.setAdapter(new MusicListAdapter(filteredSongList, getApplicationContext()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * checks if access to external storage is granted
     * @return boolean
     */
    boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    /**
     * request permission from user for access to external storage if not done so already
     */
    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            Toast.makeText(MainActivity.this, "Access to external storage is required to find songs", Toast.LENGTH_SHORT).show();
        else
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        // Connect to app remote
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");
                        // Start interacting with app remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                        // Connection failed, handle errors here
                    }
                });
    }

    /**
     * Behavior of Spotify app remote once connected
     */
    private void connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Toast.makeText(this, track.name + " by " + track.artist.name, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect app remote
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}