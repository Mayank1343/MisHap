package com.example.mishappawarenessapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mishappawarenessapp.MediaFeedAdapter
import com.example.mishappawarenessapp.R
import com.example.mishappawarenessapp.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var onCommentClick: ((String) -> Unit)? = null


    // ---------------- VIEW HOLDER ----------------
    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.username)
        val content: TextView = view.findViewById(R.id.contentText)
        val upvotes: TextView = view.findViewById(R.id.upvotes)
        val downvotes: TextView = view.findViewById(R.id.downvotes)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
        val mediaRecycler: RecyclerView = view.findViewById(R.id.postMediaRecycler)

        val commentBtn: View = view.findViewById(R.id.commentBtn)
        val commentCount: TextView = view.findViewById(R.id.commentCount)




    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // -------- BASIC DATA --------
        holder.username.text = post.username
        holder.content.text = post.content
        holder.upvotes.text = "↑ ${post.likes}"
        holder.downvotes.text = "↓ ${post.dislikes}"

        holder.commentCount.text = (post.commentCount ?: 0).toString()

        holder.commentBtn.setOnClickListener {
            onCommentClick?.invoke(post.id)
        }

        post.timestamp?.let {
            holder.timestamp.text = it.toDate().toString()
        }

    // -------- LIKE --------
        holder.upvotes.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            if (post.likedBy.contains(userId)) return@setOnClickListener

            val postRef = firestore.collection("posts").document(post.id)
            val updates = hashMapOf<String, Any>(
                "likes" to FieldValue.increment(1),
                "likedBy" to FieldValue.arrayUnion(userId)
            )

            if (post.dislikedBy.contains(userId)) {
                updates["dislikes"] = FieldValue.increment(-1)
                updates["dislikedBy"] = FieldValue.arrayRemove(userId)
            }

            postRef.update(updates)
        }

        // -------- DISLIKE --------
        holder.downvotes.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            if (post.dislikedBy.contains(userId)) return@setOnClickListener

            val postRef = firestore.collection("posts").document(post.id)
            val updates = hashMapOf<String, Any>(
                "dislikes" to FieldValue.increment(1),
                "dislikedBy" to FieldValue.arrayUnion(userId)
            )

            if (post.likedBy.contains(userId)) {
                updates["likes"] = FieldValue.increment(-1)
                updates["likedBy"] = FieldValue.arrayRemove(userId)
            }

            postRef.update(updates)
        }

        // -------- MEDIA FEED (FIXED PART ) --------
        if (post.media.isNotEmpty()) {
            holder.mediaRecycler.visibility = View.VISIBLE

            holder.mediaRecycler.layoutManager =
                LinearLayoutManager(
                    holder.itemView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            holder.mediaRecycler.adapter =
                MediaFeedAdapter(mediaList = post.media)

        } else {
            holder.mediaRecycler.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size
}