package com.example.mishappawarenessapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mishappawarenessapp.models.PostMedia

class MediaFeedAdapter(
    private val mediaList: List<PostMedia>
) : RecyclerView.Adapter<MediaFeedAdapter.MediaViewHolder>() {

    // ---------------- VIEW HOLDER ----------------
    class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.feedMediaImage)
        val playIcon: ImageView = view.findViewById(R.id.playIcon)
    }

    // ---------------- CREATE VIEW ----------------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_media, parent, false)
        return MediaViewHolder(view)
    }

    // ---------------- BIND DATA ----------------
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]

        Glide.with(holder.itemView.context)
            .load(media.url)
            .centerCrop()
            .into(holder.image)

        holder.playIcon.visibility =
            if (media.type == "video") View.VISIBLE else View.GONE
    }

    // ---------------- ITEM COUNT ----------------
    override fun getItemCount(): Int = mediaList.size
}
