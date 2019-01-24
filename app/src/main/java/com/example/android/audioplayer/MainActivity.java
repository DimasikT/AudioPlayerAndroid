package com.example.android.audioplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
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
import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SongSubscriber {

    private static final int REQUEST_CODE_PERMISSION = 1;
    static final private int CHOOSE_SONG = 0;

    private MediaPlayer mediaPlayer;

    private ImageView play;

    private SeekBar progressSeekBar;

    private SongService songs;

    private TextView songNameTextView;

    private TextView durationStartTextView;
    private TextView durationEndTextView;

    private Timer timer;

    private BlastVisualizer blastV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFields();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            requestPermission();
        } else {
            songs.findSongs();
            songs.start();
            mediaPlayer = songs.getMediaPlayer();
            tuneProgressSeekBar(mediaPlayer, songs.getNowPlaying());
            blastV = findViewById(R.id.blast);
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId != -1) blastV.setAudioSessionId(audioSessionId);
        }
        songs.addSongSubscriber(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (blastV != null)
            blastV.release();
        songs.removeSongSubscriber(this);
        if(mediaPlayer != null){
            songs.close();
        }
    }

    private void initFields() {
        songs = SongService.getInstance();
        songs.setContext(getApplicationContext());
        play = findViewById(R.id.play_imageView);
        progressSeekBar = findViewById(R.id.progressSeekBar);
        songNameTextView = findViewById(R.id.nowPlaying_textView);
        durationStartTextView = findViewById(R.id.duration_start_textView);
        durationEndTextView = findViewById(R.id.duration_end_textView);

        timer = new Timer();
        songNameTextView.setSelected(true);
    }


    private void tuneProgressSeekBar(final MediaPlayer mediaPlayer, Song song) {
        songNameTextView.setText(song.getName());
        durationStartTextView.setText("00:00");
        durationEndTextView.setText(parseDuration(song.getDuration()));

        progressSeekBar.setMax(mediaPlayer.getDuration());
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                progressSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                durationStartTextView.setText(parseDuration(mediaPlayer.getCurrentPosition()));
            }
        }, 0, 1000);


        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    durationStartTextView.setText(parseDuration(progress));
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
        animateButton(view);
        if(mediaPlayer == null) {
            return;
        }
        songs.skipPrevious();
        reTuneSeekBar();
    }

    public void playPause(View view) {
        animateButton(view);
        if(mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            play.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            play.setImageResource(R.drawable.ic_pause);
        }
    }

    public void skipNext(View view) {
        animateButton(view);
        if(mediaPlayer == null) {
            return;
        }
        songs.skipNext();
        reTuneSeekBar();
    }

    public void stop(View view) {
        animateButton(view);
        if(mediaPlayer == null) {
            return;
        }
        mediaPlayer.stop();
        try {
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
        play.setImageResource(R.drawable.ic_play);
    }

    public void showList(View view) {
        Intent songsListIntent = new Intent(MainActivity.this, SongListActivity.class);
        startActivityForResult(songsListIntent, CHOOSE_SONG);
    }

    private void reTuneSeekBar() {
        timer.cancel();
        tuneProgressSeekBar(mediaPlayer, songs.getNowPlaying());
    }

    private void animateButton(View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate().scaleX(1f).scaleY(1f).setDuration(200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        songs.findSongs();
                        songs.start();
                        mediaPlayer = songs.getMediaPlayer();
                        tuneProgressSeekBar(mediaPlayer, songs.getNowPlaying());
                    } else {
                        progressSeekBar.setEnabled(false);
                        durationStartTextView.setText("00:00");
                        durationEndTextView.setText("00:00");
                        return;
                    }

                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        blastV = findViewById(R.id.blast);
                        int audioSessionId = mediaPlayer.getAudioSessionId();
                        if (audioSessionId != -1) blastV.setAudioSessionId(audioSessionId);
                    }
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_SONG) {
            if (resultCode == RESULT_OK) {
                String songName = data.getStringExtra("songName");
                songs.playForName(songName);
                reTuneSeekBar();
            }
        }
    }

    private String parseDuration(int ms) {
        if(ms == -1) {
            return "?";
        } else {
            int minutes = ms/1000/60;
            int seconds = ms/1000%60;
            String min = minutes > 9 ? minutes + "" : "0" + minutes;
            String sec = seconds > 9 ? seconds + "" : "0" + seconds;

            return min + ":" + sec;
        }

    }

    @Override
    public void updateState() {
        reTuneSeekBar();
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE_PERMISSION);
    }
}
