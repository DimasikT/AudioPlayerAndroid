package com.example.android.audioplayer;



import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class SongListFragment extends Fragment implements SongSubscriber{

    private RecyclerView songRecyclerView;
    private SongAdapter songAdapter;
    private SongService songs;
    private TextView noTracksTextView;

    public static SongListFragment newInstance() {
        return new SongListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songs = SongService.getInstance();
        songs.addSongSubscriber(this);
        if(songAdapter != null){
            songAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list, container, false);
        noTracksTextView = view.findViewById(R.id.no_tracks_text_view);
        if(songs.getSongs().isEmpty()) {
            noTracksTextView.setVisibility(View.VISIBLE);
        } else {
            songRecyclerView = view.findViewById(R.id.song_recyclerView);
            songRecyclerView.setHasFixedSize(true);
            songRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            songAdapter = new SongAdapter();
            songRecyclerView.setAdapter(songAdapter);
            songAdapter.setItems(songs.getSongs());
        }
        return view;
    }

    @Override
    public void onDestroy() {
        songs.removeSongSubscriber(this);
        super.onDestroy();
    }

    @Override
    public void updateState() {
        songAdapter.notifyDataSetChanged();
    }

}
