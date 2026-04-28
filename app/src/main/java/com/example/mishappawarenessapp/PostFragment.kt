package com.example.mishappawarenessapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import supabase.uploadPostMedia
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect

class PostFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedLocation: GeoPoint? = null

    private val selectedMedia = mutableListOf<Uri>()
    private val uploadedMedia = mutableListOf<Map<String, String>>()
    private val MAX_MEDIA = 5

    private lateinit var mediaAdapter: MediaPreviewAdapter
    private lateinit var mediaCountText: TextView
    private lateinit var postButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var locationStatusText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_post, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val postContent = view.findViewById<EditText>(R.id.postContent)
        val addMediaButton = view.findViewById<Button>(R.id.btnAddMedia)
        val addLocationButton = view.findViewById<Button>(R.id.btnAddLocation)
        val mediaRecycler = view.findViewById<RecyclerView>(R.id.mediaRecycler)
        postButton = view.findViewById(R.id.btnPost)

        progressBar = view.findViewById(R.id.uploadProgressBar)
        progressText = view.findViewById(R.id.uploadProgressText)
        mediaCountText = view.findViewById(R.id.mediaCountText)
        locationStatusText = view.findViewById(R.id.locationStatusText)

        mediaAdapter = MediaPreviewAdapter(selectedMedia) { index ->
            selectedMedia.removeAt(index)
            mediaAdapter.notifyDataSetChanged()
            mediaCountText.text = "${selectedMedia.size} / 5 selected"
        }

        mediaRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mediaRecycler.adapter = mediaAdapter

        addMediaButton.setOnClickListener { requestMediaPermission() }

        addLocationButton.setOnClickListener { fetchCurrentLocation() }

        postButton.setOnClickListener {
            val content = postContent.text.toString().trim()
            if (content.isEmpty()) {
                postContent.error = "Post cannot be empty"
                return@setOnClickListener
            }
            uploadMediaAndCreatePost(content)
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                selectedLocation = GeoPoint(location.latitude, location.longitude)
                locationStatusText.text = "Location Attached ✓"
                locationStatusText.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "Could not get location. Turn on GPS.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) fetchCurrentLocation()
    }

    private fun uploadMediaAndCreatePost(content: String) {
        val currentUser = auth.currentUser ?: return
        setUploadingState(true)
        uploadedMedia.clear()

        lifecycleScope.launch {
            try {
                selectedMedia.forEach { uri ->
                    val type = if (isVideo(uri)) "video" else "image"
                    val file = if (type == "image") uriToCompressedFile(uri) else uriToFile(uri)
                    val url = uploadPostMedia(file = file, userId = currentUser.uid)
                    if (url != null) uploadedMedia.add(mapOf("url" to url, "type" to type))
                }
                savePost(content, uploadedMedia)

                val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))

            } catch (e: Exception) {
                Log.e("PostFragment", "Upload failed", e)
                setUploadingState(false)
            }
        }
    }

    private fun savePost(content: String, media: List<Map<String, String>>) {
        val user = auth.currentUser ?: return
        val post = hashMapOf(
            "userId" to user.uid,
            "username" to (user.email ?: "Anonymous"),
            "content" to content,
            "media" to media,
            "location" to selectedLocation, // Saves as GeoPoint for Map compatibility
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("posts").add(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post Shared!", Toast.LENGTH_SHORT).show()
                setUploadingState(false)
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { setUploadingState(false) }
    }

    private fun setUploadingState(uploading: Boolean) {
        postButton.isEnabled = !uploading
        progressBar.visibility = if (uploading) View.VISIBLE else View.GONE
        progressText.visibility = if (uploading) View.VISIBLE else View.GONE
    }

    private val mediaPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.any { it.value }) mediaPicker.launch(arrayOf("image/*", "video/*"))
    }

    private fun requestMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO))
        } else {
            mediaPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private val mediaPicker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult
        selectedMedia.addAll(uris.take(MAX_MEDIA - selectedMedia.size))
        mediaAdapter.notifyDataSetChanged()
        mediaCountText.text = "${selectedMedia.size} / 5 selected"
    }

    private fun isVideo(uri: Uri): Boolean = requireContext().contentResolver.getType(uri)?.startsWith("video/") == true

    private fun isVideoTooLong(uri: Uri): Boolean {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireContext(), uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            retriever.release()
            duration > 90_000
        } catch (e: Exception) { false }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".tmp", requireContext().cacheDir)
        tempFile.outputStream().use { inputStream?.copyTo(it) }
        return tempFile
    }

    private fun uriToCompressedFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val compressedFile = File.createTempFile("comp_", ".jpg", requireContext().cacheDir)
        val out = FileOutputStream(compressedFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        out.close()
        return compressedFile
    }
}