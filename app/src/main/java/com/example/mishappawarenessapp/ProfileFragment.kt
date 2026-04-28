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
import com.example.mishappawarenessapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import supabase.uploadPostMedia

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // MODERN WAY: Image Picker Launcher
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadProfilePicture(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserData()

        binding.btnUploadPic.setOnClickListener {
            // Launch the picker
            imagePickerLauncher.launch("image/*")
        }

        binding.btnEditProfile.setOnClickListener { showEditDialog() }
        binding.btnLogout.setOnClickListener { logoutUser() }

        return binding.root
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                binding.txtName.text = "Name: ${it.getString("name")}"
                binding.txtEmail.text = "Email: ${it.getString("email")}"
                binding.txtBio.text = "Bio: ${it.getString("bio") ?: "Not set"}"

                val photoUrl = it.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    Picasso.get().load(photoUrl)
                        .placeholder(R.drawable.ic_profile) // Add a default icon here
                        .resize(300,300)
                        .centerCrop()
                        .into(binding.profileImage)
                }
            }
    }


    private fun uploadProfilePicture(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        // Show user that work is starting
        Toast.makeText(requireContext(), "Uploading to Supabase...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // 1. Convert Uri to File so Supabase can read it
                val file = uriToFile(uri)

                // 2. Call your Supabase function
                // This uses the code you pasted above
                val downloadUrl = uploadPostMedia(file, uid)

                if (downloadUrl != null) {
                    // 3. Update the Firestore "photoUrl" field
                    db.collection("users").document(uid)
                        .update("photoUrl", downloadUrl)
                        .addOnSuccessListener {
                            // 4. Success! Refresh the UI
                            Picasso.get().invalidate(downloadUrl)
                            Picasso.get().load(downloadUrl)
                                .resize(300, 300)
                                .centerCrop()
                                .into(binding.profileImage)

                            Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Firestore Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: Exception) {
                Log.e("SupabaseError", "Upload failed: ${e.message}")
                Toast.makeText(requireContext(), "Upload Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Helper function to convert Uri to File (Required for Supabase)
    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_profile_", ".jpg", requireContext().cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }


    private fun showEditDialog() {
        val dialog = EditProfileDialogFragment()
        dialog.show(parentFragmentManager, "EditProfileDialog")
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }
}