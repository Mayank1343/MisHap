package com.example.mishappawarenessapp

import android.Manifest
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import supabase.uploadPostMedia
import android.graphics.BitmapFactory
import android.graphics.Bitmap


class PostFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val selectedMedia = mutableListOf<Uri>()
    private val MAX_MEDIA = 5

    private var mediaAdapter: MediaPreviewAdapter? = null

    private lateinit var mediaCountText: TextView
    private lateinit var postButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    /* ---------------- VIEW ---------------- */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val postContent = view.findViewById<EditText>(R.id.postContent)
        val addMediaButton = view.findViewById<Button>(R.id.btnAddMedia)
        postButton = view.findViewById(R.id.btnPost)
        progressBar = view.findViewById(R.id.uploadProgressBar)
        progressText = view.findViewById(R.id.uploadProgressText)

        mediaCountText = view.findViewById(R.id.mediaCountText)
        val mediaRecycler = view.findViewById<RecyclerView>(R.id.mediaPreviewRecycler)

        /* -------- MEDIA PREVIEW SETUP (CRITICAL) -------- */

        mediaAdapter = MediaPreviewAdapter(selectedMedia) { index ->
            selectedMedia.removeAt(index)
            mediaAdapter?.notifyDataSetChanged()
            mediaCountText.text = "${selectedMedia.size} / 5 selected"
        }

        mediaRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mediaRecycler.adapter = mediaAdapter

        mediaCountText.text = "0 / 5 selected"

        /* -------- BUTTONS -------- */

        addMediaButton.setOnClickListener {
            requestMediaPermission()
        }

        postButton.setOnClickListener {
            val content = postContent.text.toString().trim()
            if (content.isEmpty()) {
                postContent.error = "Post cannot be empty"
                return@setOnClickListener
            }
            uploadMediaAndCreatePost(content)
        }
    }

    /* ---------------- UI STATE ---------------- */

    private fun setUploadingState(uploading: Boolean) {
        postButton.isEnabled = !uploading
        progressBar.visibility = if (uploading) View.VISIBLE else View.GONE
        progressText.visibility = if (uploading) View.VISIBLE else View.GONE
    }

    /* ---------------- PERMISSIONS ---------------- */

    private val mediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.any { it.value }) {
                mediaPicker.launch(arrayOf("image/*", "video/*"))
            } else {
                Toast.makeText(requireContext(), "Media permission required", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            mediaPermissionLauncher.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }

    /* ---------------- MEDIA PICKER ---------------- */

    private val mediaPicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->

            if (uris.isEmpty()) return@registerForActivityResult

            if (selectedMedia.size + uris.size > MAX_MEDIA) {
                Toast.makeText(requireContext(), "Max 5 media allowed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            uris.forEach { uri ->
                if (isVideo(uri) && isVideoTooLong(uri)) {
                    Toast.makeText(
                        requireContext(),
                        "Video must be under 90 seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@registerForActivityResult
                }
            }

            selectedMedia.addAll(uris)
            mediaAdapter?.notifyDataSetChanged()
            mediaCountText.text = "${selectedMedia.size} / 5 selected"
        }

    /* ---------------- UPLOAD + POST ---------------- */

    private fun uploadMediaAndCreatePost(content: String) {
        val currentUser = auth.currentUser ?: return

        setUploadingState(true)
        progressBar.progress = 0
        progressText.text = "Uploading 0%"

        val uploadedMedia = mutableListOf<Map<String, String>>()

        if (selectedMedia.isEmpty()) {
            savePost(content, uploadedMedia)
            return
        }

        lifecycleScope.launch {
            try {
                selectedMedia.forEach { uri ->
                    val type = if (isVideo(uri)) "video" else "image"

                    val file = uriToCompressedFile(uri)
                    val url = uploadPostMedia(
                        file = file,
                        userId = currentUser.uid
                    )

                    uploadedMedia.add(
                        mapOf(
                            "url" to url,
                            "type" to type
                        )
                    )

                    val percent = (uploadedMedia.size * 100) / selectedMedia.size
                    progressBar.progress = percent
                    progressText.text = "Uploading $percent%"
                }

                savePost(content, uploadedMedia)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                setUploadingState(false)
            }
        }
    }


    private fun savePost(content: String, media: List<Map<String, String>>) {
        val currentUser = auth.currentUser ?: return

        val post = hashMapOf(
            "userId" to currentUser.uid,
            "username" to (currentUser.email ?: "Anonymous"),
            "content" to content,
            "media" to media,
            "likes" to 0,
            "dislikes" to 0,
            "likedBy" to emptyList<String>(),
            "dislikedBy" to emptyList<String>(),
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post published", Toast.LENGTH_SHORT).show()
                setUploadingState(false)
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                setUploadingState(false)
            }
    }

    /* ---------------- HELPERS ---------------- */

    private fun isVideo(uri: Uri): Boolean {
        val type = requireContext().contentResolver.getType(uri)
        return type?.startsWith("video/") == true
    }

    private fun isVideoTooLong(uri: Uri): Boolean {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireContext(), uri)
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong() ?: 0
            retriever.release()
            duration > 90_000
        } catch (e: Exception) {
            false
        }
    }



    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".tmp", requireContext().cacheDir)

        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }

        return tempFile
    }

    private fun uriToCompressedFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val originalFile = File.createTempFile("orig_", ".jpg", requireContext().cacheDir)

        originalFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }

        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)

        val compressedFile =
            File.createTempFile("compressed_", ".jpg", requireContext().cacheDir)

        compressedFile.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }

        return compressedFile
    }


}