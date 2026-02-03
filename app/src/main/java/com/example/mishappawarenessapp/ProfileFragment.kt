package com.example.mishappawarenessapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mishappawarenessapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import supabase.uploadProfileImage
import java.io.File
import android.util.Log

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var imageUri: Uri? = null
    private var isUploading = false

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && !isUploading && isAdded) {
                imageUri = uri
                uploadProfilePicture()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserData()

        binding.btnUploadPic.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnEditProfile.setOnClickListener { showEditDialog() }
        binding.btnLogout.setOnClickListener { logoutUser() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                binding.txtName.text = doc.getString("name") ?: ""
                binding.txtEmail.text = doc.getString("email") ?: ""
                binding.txtBio.text = doc.getString("bio") ?: ""

                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrBlank()) {
                    Picasso.get()
                        .load(photoUrl)
                        .into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(
                        R.drawable.ic_profile
                    )
                }
            }
    }


    private fun uriToFile(uri: Uri): File {
        val ctx = context ?: throw IllegalStateException("Context null")

        val inputStream = ctx.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open image")

        val file = File(ctx.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { inputStream.copyTo(it) }
        return file
    }

    private fun uploadProfilePicture() {
        val uid = auth.currentUser?.uid ?: return
        val uri = imageUri ?: return

        isUploading = true

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = uriToFile(uri)
                val imageUrl = uploadProfileImage(file, uid)

                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext

                    db.collection("users")
                        .document(uid)
                        .update("photoUrl", imageUrl)

                    Picasso.get().load(imageUrl).into(binding.profileImage)

                    context?.let {
                        Toast.makeText(it, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                isUploading = false
            }
        }
        Log.d("UPLOAD_PROFILE", "uploadProfilePicture() called")

    }

    private fun showEditDialog() {
        if (isAdded) {
            EditProfileDialogFragment()
                .show(parentFragmentManager, "EditProfileDialog")
        }
    }

    private fun logoutUser() {
        auth.signOut()
        context?.let {
            startActivity(Intent(it, LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}
