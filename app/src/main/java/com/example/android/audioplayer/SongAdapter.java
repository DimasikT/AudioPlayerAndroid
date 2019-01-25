package com.example.android.audioplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.audioplayer.model.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList = new ArrayList<>();

    private SongService songService = SongService.getInstance();

    public void setItems(Collection<Song> songs) {
        songList.addAll(songs);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_song_layout, viewGroup, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder songViewHolder, int i) {
            songViewHolder.bind(songList.get(i), songService.getMediaPlayer().isPlaying());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        // Ваш ViewHolder должен содержать переменные для всех
        // View-компонентов, которым вы хотите задавать какие-либо свойства
        // в процессе работы пользователя со списком

        private TextView nameSongTextView;
        private ImageView songImageView;
        private TextView durationTextView;
        private EqualizerView equalizer;


        private void bind(Song song, boolean isPlaying){

            nameSongTextView.setText(song.getName());
            durationTextView.setText(parseDuration(song.getDuration()));
            if(song.isSelected()){
                markSongAsSelected();
                if(isPlaying){
                    songImageView.setImageResource(R.drawable.ic_play_now);
                    equalizer.animateBars();
                } else {
                    equalizer.stopBars();
                }
            } else {
                markSongAsNotSelected();
            }
        }

        // Мы также создали конструктор, который принимает на вход View-компонент строкИ
        // и ищет все дочерние компоненты
        public SongViewHolder(View itemView) {
            super(itemView);
            nameSongTextView = itemView.findViewById(R.id.name_song_textView);
            songImageView = itemView.findViewById(R.id.song_image_view);
            durationTextView = itemView.findViewById(R.id.duration_song_textView);
            equalizer = itemView.findViewById(R.id.song_list_equalizer_view);

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

        private void markSongAsSelected(){
            songImageView.setImageResource(R.drawable.ic_audiotrack_selected);
            nameSongTextView.setTextColor(0xFF478dff);
            nameSongTextView.setSelected(true);
            durationTextView.setVisibility(View.INVISIBLE);
            equalizer.setVisibility(View.VISIBLE);
        }

        private void markSongAsNotSelected(){
            songImageView.setImageResource(R.drawable.ic_audiotrack);
            nameSongTextView.setTextColor(0xffffffff);
            nameSongTextView.setSelected(false);
            durationTextView.setVisibility(View.VISIBLE);
            equalizer.setVisibility(View.INVISIBLE);
        }
    }
}
