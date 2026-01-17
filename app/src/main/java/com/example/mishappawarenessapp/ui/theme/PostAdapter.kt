package com.example.mishappawarenessapp.ui.home

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.mishappawarenessapp.R
import com.example.mishappawarenessapp.model.Post



class PostAdapter(private val posts: List<Post>)
    : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // Holds references to views (performance)
    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.username)
        val content: TextView = view.findViewById(R.id.contentText)
        val image: ImageView = view.findViewById(R.id.postImage)
        val upvotes: TextView = view.findViewById(R.id.upvotes)
        val downvotes: TextView = view.findViewById(R.id.downvotes)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.username.text = post.username
        holder.content.text = post.content
        holder.upvotes.text = "⬆ ${post.upvotes}"
        holder.downvotes.text = "⬇ ${post.downvotes}"
        holder.timestamp.text = post.timestamp

        if (post.imageRes != null) {
            holder.image.visibility = View.VISIBLE
            holder.image.setImageResource(post.imageRes)
        } else {
            holder.image.visibility = View.GONE
        }
    }

    override fun getItemCount() = posts.size
}
