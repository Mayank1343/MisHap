package com.example.mishappawarenessapp

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class CommentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun addComment(
        postId: String,
        commentText: String,
        userId: String,
        username: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val postRef = firestore.collection("posts").document(postId)

        val comment = hashMapOf(
            "commentText" to commentText,
            "userId" to userId,
            "username" to username,
            "timestamp" to System.currentTimeMillis()
        )

        postRef
            .collection("comments")
            .add(comment)
            .addOnSuccessListener {
                postRef.update(
                    "commentCount",
                    FieldValue.increment(1)
                )
            }
    }

}
