package com.example.mishappawarenessapp.model

import com.example.mishappawarenessapp.models.PostMedia
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Post(
    var id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    var media: List<PostMedia> = emptyList(),
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList(),
    val timestamp: Timestamp? = null,
    var commentCount: Long? = 0,
    var location: GeoPoint? = null
)