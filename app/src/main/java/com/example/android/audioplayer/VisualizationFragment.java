package com.example.android.audioplayer;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class VisualizationFragment extends Fragment implements SongSubscriber {

    private SongService songs;

    private BlastVisualizer blastV;

    private TextView songNameTextView;

    private MediaPlayer mediaPlayer;


    public static VisualizationFragment newInstance() {
        return new VisualizationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songs = SongService.getInstance();
        mediaPlayer = songs.getMediaPlayer();
        songs.addSongSubscriber(this);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visualization, container, false);

        songNameTextView = view.findViewById(R.id.nowPlaying_textView);
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            blastV = view.findViewById(R.id.blast);
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId != -1) blastV.setAudioSessionId(audioSessionId);
        }

        songNameTextView.setText(songs.getNowPlaying().getName());
        songNameTextView.setSelected(true);
        return view;
    }

    @Override
    public void onDestroy() {
        if (blastV != null)
            blastV.release();
        songs.removeSongSubscriber(this);
        super.onDestroy();
    }

    @Override
    public void updateState() {
        songNameTextView.setText(songs.getNowPlaying().getName());
    }
}
