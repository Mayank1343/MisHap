package com.example.mishappawarenessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mishappawarenessapp.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentChange
import android.widget.Toast
import com.example.mishappawarenessapp.ui.home.PostAdapter

class HomeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private var isFirstLoad = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val recyclerView = view.findViewById<RecyclerView>(R.id.postRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postAdapter = PostAdapter(postList)
        recyclerView.adapter = postAdapter


        postAdapter.notifyDataSetChanged()


        fetchPosts()

    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) return@addSnapshotListener

                // Detect new post only AFTER first load
                if (!isFirstLoad && snapshots.documentChanges.any {
                        it.type == DocumentChange.Type.ADDED
                    }) {
                    Toast.makeText(
                        requireContext(),
                        "Post published",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                postList.clear()
                for (doc in snapshots) {
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id
                    postList.add(post)

                }

                postAdapter.notifyDataSetChanged()

                isFirstLoad = false
            }
    }

}