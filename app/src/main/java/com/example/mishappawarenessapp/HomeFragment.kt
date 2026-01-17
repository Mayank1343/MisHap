package com.example.mishappawarenessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mishappawarenessapp.model.Post
import com.example.mishappawarenessapp.ui.home.PostAdapter

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1️⃣ Get RecyclerView from layout
        val recyclerView = view.findViewById<RecyclerView>(R.id.postRecycler)

        // 2️⃣ Dummy data (simulating backend)
        val posts = listOf(
            Post(
                username = "mayank",
                content = "First post on Mishap 🚀",
                imageRes = null,
                upvotes = 12,
                downvotes = 1,
                timestamp = "1h ago"
            ),
            Post(
                username = "rahul",
                content = "Awareness is important",
                imageRes = R.drawable.sample_image,
                upvotes = 20,
                downvotes = 2,
                timestamp = "2h ago"
            )
        )

        // 3️⃣ RecyclerView setup
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = PostAdapter(posts)
    }
}
