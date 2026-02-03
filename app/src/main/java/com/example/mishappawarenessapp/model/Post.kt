package com.example.mishappawarenessapp.model

import com.example.mishappawarenessapp.models.PostMedia
import com.google.firebase.Timestamp

data class Post(
    var id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    var media: List<PostMedia> = emptyList(),   // to store media

    // 📍 Location data
    val latitude: Double? = null,
    val longitude: Double? = null,

    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),     // storing people who liked
    val dislikedBy: List<String> = emptyList(),  // storing people who disliked
    val timestamp: Timestamp? = null,
    var commentCount: Long? = 0
)
