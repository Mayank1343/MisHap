package com.example.mishappawarenessapp.model


data class Post(
    val username: String,      // who posted
    val content: String,       // text content
    val imageRes: Int?,        // optional image
    val upvotes: Int,          // likes
    val downvotes: Int,        // dislikes
    val timestamp: String      // time info
)
