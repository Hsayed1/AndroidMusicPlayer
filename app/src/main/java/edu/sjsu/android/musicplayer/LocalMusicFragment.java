package edu.sjsu.android.musicplayer;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import edu.sjsu.android.musicplayer.databinding.FragmentLocalMusicBinding;

public class LocalMusicFragment extends Fragment {
    FragmentLocalMusicBinding binding;
    List<Audio> filteredSongList = new ArrayList<>();
    List<Audio> originalSongList = new ArrayList<>();

    public static LocalMusicFragment newInstance() {
        LocalMusicFragment fragment = new LocalMusicFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("test", 1);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLocalMusicBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.noLocalSongs.setVisibility(View.INVISIBLE);

        if(!checkPermission()){
            requestPermission();
            return root;
        }
        loadSongs();

        return root;

    }

    public void loadSongs() {
        String[] projection = {MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION };
        //passing because we only want music from the database
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        while(cursor.moveToNext()){
            //reading database and creating an audio object from the data
            Audio song = new Audio(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if(new File(song.getPath()).exists()) //checking to see if the song exists then adding it to the list
                originalSongList.add(song);
        }

        filteredSongList = new ArrayList<>(originalSongList);
        if(filteredSongList.size() == 0) //if there are no songs show message
            binding.noLocalSongs.setVisibility(View.VISIBLE);
        else {
            //setting the recycler view
            binding.list.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.list.setAdapter(new MusicListAdapter(filteredSongList, getContext()));
        }
    }

    public void handleSearch(String searchText) {
        if(searchText.isEmpty()){
            binding.noLocalSongs.setVisibility(View.VISIBLE);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !searchText.isEmpty()) {
            filteredSongList = originalSongList
                    .stream()
                    .filter(song -> song
                            .getTitle()
                            .toLowerCase()
                            .contains(searchText)
                    ).collect(Collectors.toList());
            binding.list.setAdapter(new MusicListAdapter(filteredSongList, getContext()));
        }
    }

    boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE))
            Toast.makeText(getContext(), "Access to external storage is required to find songs", Toast.LENGTH_SHORT).show();
        else
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
    }
}