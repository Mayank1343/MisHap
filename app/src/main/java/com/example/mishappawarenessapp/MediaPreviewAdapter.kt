package com.example.mishappawarenessapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaPreviewAdapter(
    private val mediaList: MutableList<Uri>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<MediaPreviewAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_preview, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val uri = mediaList[position]

        // Load image / video thumbnail
        Glide.with(holder.itemView.context)
            .load(uri)
            .centerCrop()
            .into(holder.previewImage)

        // Show play icon for videos
        val mimeType = holder.itemView.context.contentResolver.getType(uri)
        holder.playIcon.visibility =
            if (mimeType?.startsWith("video/") == true) View.VISIBLE else View.GONE

        // Remove media
        holder.removeBtn.setOnClickListener {
            onRemove(position)
        }
    }

    override fun getItemCount(): Int = mediaList.size

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val previewImage: ImageView = itemView.findViewById(R.id.mediaPreviewImage)
        val playIcon: ImageView = itemView.findViewById(R.id.videoPlayIcon)
        val removeBtn: ImageView = itemView.findViewById(R.id.removeMediaBtn)
    }
}