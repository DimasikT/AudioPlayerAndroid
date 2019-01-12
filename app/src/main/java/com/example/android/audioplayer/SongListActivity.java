package com.example.android.audioplayer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


public class SongListActivity extends AppCompatActivity {

    private RecyclerView songRecyclerView;
    private SongAdapter songAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitvity_songlist);

        setTitle(R.string.song_list_title);

        initRecyclerView();
        Intent songsList = getIntent();
        loadSongList(songsList);
    }

    private void loadSongList(Intent intent) {

        SongsDTO songsDTO = (SongsDTO) intent.getSerializableExtra("songsList");

        songAdapter.setItems(songsDTO.getSongs());
    }

    private void initRecyclerView() {
        songRecyclerView = findViewById(R.id.song_recyclerView);
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
}
