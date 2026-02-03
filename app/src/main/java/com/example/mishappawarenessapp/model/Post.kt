package com.example.mishappawarenessapp.model

import com.example.mishappawarenessapp.models.PostMedia


data class Post(
    var id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    var media: List<PostMedia> = emptyList(),   // to store media
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),     // storing peoples who likes in array
    val dislikedBy: List<String> = emptyList(),   // storing disliked people in array. (to make interaction unique)
    val timestamp: com.google.firebase.Timestamp? = null,
    var commentCount: Long? = 0
)