package com.example.android.audioplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.audioplayer.model.Song;
import com.example.android.audioplayer.model.SongsDTO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION_READ_CONTACTS = 1;
    static final private int CHOOSE_SONG = 0;

    private MediaPlayer mediaPlayer;

    private ImageView play;

    private SeekBar progressSeekBar;

    private List<Song> songs;
    private int nowPlayingIndex;

    private TextView songNameTextView;

    private Timer timer;

    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFields();

        if(checkPermission()){
            songs = getAllAudioFromDevice(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_READ_CONTACTS);
        }
        if(!songs.isEmpty()){
            playFirst();
        }

        tuneProgressSeekBar(mediaPlayer);
    }


    private void initFields() {
        play = findViewById(R.id.play_imageView);

        progressSeekBar = findViewById(R.id.progressSeekBar);

        songNameTextView = findViewById(R.id.nowPlaying_textView);

        timer = new Timer();
    }

    private void tuneMediaPlayer(Song song) {
        if(songs == null){
            return;
        }
        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(song.getFile()));
        songNameTextView.setText(song.getName());
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlay();
                nowPlayingIndex++;
                if (nowPlayingIndex >= songs.size()){
                    nowPlayingIndex = 0;
                    tuneMediaPlayer(songs.get(nowPlayingIndex));
                } else {
                    tuneMediaPlayer(songs.get(nowPlayingIndex));
                }
                reTuneSeekBar();
                mediaPlayer.start();
                play.setImageResource(R.drawable.ic_pause);
            }
        });
    }

    private void tuneProgressSeekBar(final MediaPlayer mediaPlayer) {
        progressSeekBar.setMax(mediaPlayer.getDuration());
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                progressSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        }, 0, 1000);



        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    public void skipPrevious(View view) {
        isPlaying = mediaPlayer.isPlaying();
        stopPlay();
        nowPlayingIndex--;
        if (nowPlayingIndex < 0){
            nowPlayingIndex = songs.size() - 1;
            tuneMediaPlayer(songs.get(nowPlayingIndex));
        } else {
            tuneMediaPlayer(songs.get(nowPlayingIndex));
        }
        reTuneSeekBar();
        if(isPlaying){ playPause(view); }
    }

    public void playPause(View view) {
        animateButton(view);
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            play.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            play.setImageResource(R.drawable.ic_pause);
        }
    }

    public void skipNext(View view) {
        isPlaying = mediaPlayer.isPlaying();
        stopPlay();
        nowPlayingIndex++;
        if (nowPlayingIndex >= songs.size()){
            nowPlayingIndex = 0;
            tuneMediaPlayer(songs.get(nowPlayingIndex));
        } else {
            tuneMediaPlayer(songs.get(nowPlayingIndex));
        }
        reTuneSeekBar();
        if(isPlaying){ playPause(view); }

    }

    public void stop(View view) {
        animateButton(view);
        stopPlay();
    }

    private void stopPlay(){
        mediaPlayer.stop();
        play.setImageResource(R.drawable.ic_play);
        try {
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
        }
        catch (Throwable t) {
            Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showList(View view) {
        Intent songsListIntent = new Intent(MainActivity.this, SongListActivity.class);
        SongsDTO songsList = new SongsDTO(songs);
        songsListIntent.putExtra("songsList", songsList);
        startActivityForResult(songsListIntent, CHOOSE_SONG);
    }

    private void reTuneSeekBar(){
        timer.cancel();
        tuneProgressSeekBar(mediaPlayer);
    }

    private void animateButton(View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate().scaleX(1f).scaleY(1f).setDuration(200);
    }

    private List<Song> getAllAudioFromDevice(final Context context) {
        final List<Song> tempSongList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE};

//        Cursor c = context.getContentResolver().query(uri, projection, MediaStore.Audio.Media.DATA + " like ? ", new String[]{"Music"}, null);
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                Song song = new Song();
                String path = c.getString(0);
                String name = c.getString(1);

                song.setName(name);
                song.setPath(path);
                song.setFile(new File(path));

                tempSongList.add(song);
            }
            c.close();
        }
        Collections.sort(tempSongList);
        return tempSongList;
    }

    public boolean checkPermission(){
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        return permissionStatus == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    songs = getAllAudioFromDevice(this);
                } else {
                    // permission denied
                    songs = Collections.emptyList();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_SONG) {
            if (resultCode == RESULT_OK) {
                isPlaying = mediaPlayer.isPlaying();
                stopPlay();
                String songName = data.getStringExtra("songName");
                for(int i = 0; i < songs.size(); i++){
                    if(songs.get(i).getName().equals(songName)){
                        tuneMediaPlayer(songs.get(i));
                        nowPlayingIndex = i;
                        reTuneSeekBar();
                        if(isPlaying){
                            mediaPlayer.start();
                            play.setImageResource(R.drawable.ic_pause);
                        }
                    }
                }
            }
        }
    }

    private void playFirst(){
        tuneMediaPlayer(songs.get(0));
        nowPlayingIndex = 0;
    }
}
