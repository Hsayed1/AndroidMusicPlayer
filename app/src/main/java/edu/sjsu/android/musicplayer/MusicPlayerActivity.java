package edu.sjsu.android.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends Activity {
    TextView titleTextView, currentTimeTextView, totalTimeTextView;
    SeekBar seekBar;
    ImageView pausePlayBtn, nextBtn, prevBtn, musicIcon;
    List<Audio> songList;
    Audio currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTextView = findViewById(R.id.song_title);
        currentTimeTextView = findViewById(R.id.current_time);
        totalTimeTextView = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlayBtn = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        prevBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon1);

        titleTextView.setSelected(true);
        songList =  (ArrayList<Audio>) getIntent().getSerializableExtra("LIST");

        setResourcesWithMusic();
        runOnUiThread(new MediaProgress());
        seekBar.setOnSeekBarChangeListener((SeekBarChangeRest) (seekBar, progress, isFromUser) -> {
            if(mediaPlayer != null && isFromUser) {
                mediaPlayer.seekTo(progress);
            }
        });
    }

    void setResourcesWithMusic() {
        currentSong = songList.get(MyMediaPlayer.currentIndex);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(currentSong.getPath());
        byte[] albumArt = retriever.getEmbeddedPicture();

        if (albumArt != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            if (bitmap != null) {
                musicIcon.setImageBitmap(bitmap);
            }
        }

        titleTextView.setText(currentSong.getTitle());
        totalTimeTextView.setText(convertToMMSS(currentSong.getLength()));
        pausePlayBtn.setOnClickListener(this::pausePlay);
        nextBtn.setOnClickListener(this::playNextSong);
        prevBtn.setOnClickListener(this::playPreviousSong);

        playMusic();
    }

    private void playMusic() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void pausePlay(View view) {
        if(mediaPlayer.isPlaying()){
            pausePlayBtn.setImageResource(R.drawable.baseline_play_arrow_24);
            mediaPlayer.pause();
        }
        else {
            pausePlayBtn.setImageResource(R.drawable.baseline_pause_24);
            mediaPlayer.start();
        }
    }

    private void playNextSong(View view) {
        if(MyMediaPlayer.currentIndex == songList.size()-1){
            return;
        }
        MyMediaPlayer.currentIndex += 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void playPreviousSong(View view) {
        if(MyMediaPlayer.currentIndex == 0){
            return;
        }
        MyMediaPlayer.currentIndex -= 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    public static String convertToMMSS(String duration) {
        long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private class MediaProgress implements Runnable {
        @Override
        public void run() {
            new Handler().postDelayed(this, 1000);
            if(mediaPlayer == null) return;
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            currentTimeTextView.setText(convertToMMSS(mediaPlayer.getCurrentPosition()+""));
        }
    }

    private interface SeekBarChangeRest extends SeekBar.OnSeekBarChangeListener {
        @Override
        default void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        default void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}