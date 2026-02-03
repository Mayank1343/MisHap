package com.example.mishappawarenessapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mishappawarenessapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserData()

        binding.btnUploadPic.setOnClickListener { selectImage() }
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
                        .resize(300,300)
                        .centerCrop()
                        .into(binding.profileImage)
                }
            }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            uploadProfilePicture()
        }
    }

    private fun uploadProfilePicture() {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profilePictures/$uid.jpg")

        imageUri?.let {
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        db.collection("users").document(uid)
                            .update("photoUrl", downloadUrl.toString())

                        Picasso.get().load(downloadUrl)
                            .into(binding.profileImage)

                        Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
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
