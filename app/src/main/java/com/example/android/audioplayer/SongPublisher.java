package com.example.android.audioplayer;

public interface SongPublisher {

    void addSongSubscriber(SongSubscriber subscriber);

    void removeSongSubscriber(SongSubscriber subscriber);

    void notifySubscribers();

}
