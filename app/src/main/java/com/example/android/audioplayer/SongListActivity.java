package com.example.android.audioplayer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.audioplayer.model.Song;

import java.util.List;


public class SongListActivity extends AppCompatActivity implements SongSubscriber {

    private RecyclerView songRecyclerView;
    private SongAdapter songAdapter;

    private SongService songs;

    private TextView noTracksTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitvity_songlist);

        noTracksTextView = findViewById(R.id.no_tracks_text_view);

        songs = SongService.getInstance();
        if(songs.getSongs().isEmpty()) {
            noTracksTextView.setVisibility(View.VISIBLE);
        } else {
            initRecyclerView();
            loadSongList(songs.getSongs());
        }
        songs.addSongSubscriber(this);
    }

    private void loadSongList(List<Song> songs) {

        songAdapter.setItems(songs);
    }

    private void initRecyclerView() {
        songRecyclerView = findViewById(R.id.song_recyclerView);
        songRecyclerView.setHasFixedSize(true);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter();
        songRecyclerView.setAdapter(songAdapter);
    }

    public void playSong(View view) {
        TextView tv = (TextView)view;
        Intent songIntent = new Intent();
        songIntent.putExtra("songName", tv.getText());
        setResult(RESULT_OK, songIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        songs.removeSongSubscriber(this);
    }

    @Override
    public void updateState() {
        recreate();
    }
}