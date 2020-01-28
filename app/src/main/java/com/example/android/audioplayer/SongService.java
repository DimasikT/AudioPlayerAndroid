package com.example.android.audioplayer;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.android.audioplayer.model.Song;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongService implements SongPublisher{

    private static SongService instance;

    private MediaPlayer mediaPlayer;

    private List<Song> songs = Collections.emptyList();

    private int nowPlayingIndex;

    private boolean isPlaying;

    private Context context;

    private List<SongSubscriber> subscribers = new ArrayList<>();



    private SongService() {

    }

    public void start(){
        if(!(songs.size() == 0)){
            Song song = songs.get(0);
            song.setSelected(true);
            tuneMediaPlayer(song);
            nowPlayingIndex = 0;
        }

    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getNowPlayingIndex() {
        return nowPlayingIndex;
    }

    public void setNowPlayingIndex(int nowPlayingIndex) {
        this.nowPlayingIndex = nowPlayingIndex;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }


    public static synchronized SongService getInstance() {
        if(instance == null) {
            instance = new SongService();
        }
        return instance;
    }

    public void findSongs() {
        songs = getAllAudioFromDevice(context);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void selectedNone() {
        for (Song s : songs) {
            s.setSelected(false);
        }
    }

    public void switchSelected() {
        Song song = songs.get(nowPlayingIndex);
        if (song.isSelected()) {
            song.setSelected(false);
        } else {
            song.setSelected(true);
        }
    }

    private List<Song> getAllAudioFromDevice(final Context context) {
        final List<Song> tempSongList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.DURATION

        };

//        Cursor c = context.getContentResolver().query(uri, projection, MediaStore.Audio.Media.DATA + " like ? ", new String[]{"Music"}, null);
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                Song song = new Song();
                String path = c.getString(0);
                String name = c.getString(1);
                int duration = Integer.parseInt(c.getString(2));

                song.setName(name);
                song.setPath(path);
                song.setDuration(duration);
                song.setFile(new File(path));

                tempSongList.add(song);
            }
            c.close();
        }
        Collections.sort(tempSongList);
        return tempSongList;
    }

    private void tuneMediaPlayer(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(context, Uri.fromFile(song.getFile()));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mediaPlayer = MediaPlayer.create(context, Uri.fromFile(song.getFile()));
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(!subscribers.isEmpty()){
                        skipNext();
                        notifySubscribers();
                        mediaPlayer.start();
                    }
                }
            });
        }

    }

    public Song getNowPlaying(){
        return songs.isEmpty() ? Song.getBlank() : songs.get(nowPlayingIndex);
    }

    public void playForName(String name) {
        isPlaying = mediaPlayer.isPlaying();
        mediaPlayer.stop();
        selectedNone();

        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getName().equals(name)) {
                tuneMediaPlayer(songs.get(i));
                nowPlayingIndex = i;
                switchSelected();
            }
        }
        if (isPlaying) {
            mediaPlayer.start();
        }
    }

    public void skipPrevious(){
        isPlaying = mediaPlayer.isPlaying();
        mediaPlayer.stop();
        switchSelected();
        nowPlayingIndex--;
        if (nowPlayingIndex < 0) {
            nowPlayingIndex = songs.size() - 1;
        }
        tuneMediaPlayer(songs.get(nowPlayingIndex));
        switchSelected();
        if(isPlaying){
            mediaPlayer.start();
        }
    }

    public void skipNext(){
        isPlaying = mediaPlayer.isPlaying();
        mediaPlayer.stop();
        switchSelected();
        nowPlayingIndex++;
        if (nowPlayingIndex >= songs.size()) {
            nowPlayingIndex = 0;
        }
        tuneMediaPlayer(songs.get(nowPlayingIndex));
        switchSelected();
        if(isPlaying){
            mediaPlayer.start();
        }
    }

    public void close(){
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    @Override
    public void addSongSubscriber(SongSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void removeSongSubscriber(SongSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void notifySubscribers() {
        for(SongSubscriber subscriber : subscribers){
            subscriber.updateState();
        }
    }
}
