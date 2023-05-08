package edu.sjsu.android.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SpotifyAdapter extends RecyclerView.Adapter<SpotifyAdapter.ViewHolder> {

    List<Track> trackList;
    Context context;
    public SpotifyAdapter(List<Track> trackList, Context context) {
        this.trackList = trackList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new SpotifyAdapter.ViewHolder(view);
    }

    /**
     * binds all data of the list to the view
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.title.setText(track.getTitle());
        Picasso.get().load(track.getImageUrl()).into(holder.music_icon);

        holder.itemView.setOnClickListener(v -> {
            Intent spotifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(track.getUri() + ":play"));
            context.startActivity(spotifyIntent);
        });
    }

    @Override
    public int getItemCount() {
        return trackList.size();
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
