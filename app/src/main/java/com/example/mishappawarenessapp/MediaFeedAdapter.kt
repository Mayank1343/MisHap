package com.example.mishappawarenessapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mishappawarenessapp.models.PostMedia

class MediaFeedAdapter(
    private val mediaList: List<PostMedia>
) : RecyclerView.Adapter<MediaFeedAdapter.MediaViewHolder>() {

    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.feedMediaImage)
        val playIcon: ImageView = view.findViewById(R.id.playIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]

        Glide.with(holder.itemView.context)
            .load(media.url)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Added for faster Supabase loading
            .centerCrop()
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .into(holder.image)

        holder.playIcon.visibility =
            if (media.type == "video") View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = mediaList.size
}