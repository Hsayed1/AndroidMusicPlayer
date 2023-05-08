package edu.sjsu.android.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    List<Audio> songList;
    Context context;
    public MusicListAdapter(List<Audio> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MusicListAdapter.ViewHolder(view);
    }

    /**
     * binds all data of the list to the view
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Audio song = songList.get(position);
        holder.title.setText(song.getTitle());

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(song.getPath());
        byte[] albumArt = retriever.getEmbeddedPicture();

        if (albumArt != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            if (bitmap != null) {
                holder.music_icon.setImageBitmap(bitmap);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            MyMediaPlayer.getInstance().reset();
            MyMediaPlayer.currentIndex = position;
            Intent intent = new Intent(context, MusicPlayerActivity.class);
            intent.putExtra("LIST", (ArrayList<Audio>) songList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        ImageView music_icon;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.music_title);
            music_icon = itemView.findViewById(R.id.music_icon);
        }
    }
}
