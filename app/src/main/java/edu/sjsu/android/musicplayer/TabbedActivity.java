package edu.sjsu.android.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import edu.sjsu.android.musicplayer.databinding.ActivityTabbedBinding;

public class TabbedActivity extends AppCompatActivity {

    private ActivityTabbedBinding binding;
    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTabbedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        handleMenuSearch();
    }

    private void handleMenuSearch() {
        binding.topAppBar.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getItemId() == R.id.search_songs){
                if(binding.searchBar.getVisibility() != View.VISIBLE){
                    binding.searchBar.setVisibility(View.VISIBLE);
                }
                else {
                    binding.searchBar.setVisibility(View.GONE);
                }
                return true;
            }
            return false;
        });

        binding.searchBarText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sectionsPagerAdapter.handleSearchText(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(sectionsPagerAdapter == null) return;
        sectionsPagerAdapter.handleSpotifyAuthorization(requestCode, resultCode, intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 123:
                sectionsPagerAdapter.loadSongs();
                break;
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        @StringRes
        private final int[] TAB_TITLES = new int[]{R.string.tab_local, R.string.tab_spotify};
        private final Context mContext;
        private LocalMusicFragment localMusicFragmentInstance;
        private SpotifyFragment spotifyFragmentInstance;


        public SectionsPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            if(localMusicFragmentInstance == null) {
                localMusicFragmentInstance = LocalMusicFragment.newInstance();
            }
            if(spotifyFragmentInstance == null) {
                spotifyFragmentInstance = SpotifyFragment.newInstance();
            }
            if(position == 0){
                return localMusicFragmentInstance;
            }
            return spotifyFragmentInstance;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getResources().getString(TAB_TITLES[position]);
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length;
        }

        public void handleSearchText(String searchText) {
            if(localMusicFragmentInstance != null) {
                findViewById(R.id.noLocalSongs).setVisibility(View.GONE);
                localMusicFragmentInstance.handleSearch(searchText);
            }
            if(spotifyFragmentInstance != null) {
                findViewById(R.id.noSpotifySongs).setVisibility(View.GONE);
                spotifyFragmentInstance.handleSearch(searchText);
            }
        }

        public void handleSpotifyAuthorization(int requestCode, int resultCode, Intent intent) {
            if(spotifyFragmentInstance == null) return;
            spotifyFragmentInstance.handleSpotifyAuthorization(requestCode, resultCode, intent);
        }

        public void loadSongs() {
            if(localMusicFragmentInstance != null) {
                localMusicFragmentInstance.loadSongs();
            }
        }
    }
}