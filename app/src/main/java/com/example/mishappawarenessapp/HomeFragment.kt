package com.example.mishappawarenessapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mishappawarenessapp.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.mishappawarenessapp.ui.home.PostAdapter

class HomeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

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

        // --- HANDLE COMMENT CLICK ---
        postAdapter.onCommentClick = { postId ->
            CommentBottomSheet
                .newInstance(postId)
                .show(parentFragmentManager, "CommentBottomSheet")
        }


        postAdapter.onLocationClick = { lat, lng ->
            val bundle = Bundle().apply {
                // Convert to string to match nav_graph argument type
                putString("targetLat", lat.toString())
                putString("targetLng", lng.toString())
            }

            try {
                findNavController().navigate(R.id.action_homeFragment_to_mapFragment, bundle)
            } catch (e: Exception) {
                Log.e("NavError", "Navigation failed: ${e.message}")
            }
        }

        fetchPosts()
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                postList.clear()
                for (doc in snapshots) {
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
    }
}