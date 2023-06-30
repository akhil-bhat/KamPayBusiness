package com.example.kampaybusiness

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: CircleImageView
    private lateinit var profileNameEditText: EditText
    private lateinit var profileAddressEditText: EditText
    private lateinit var profileSaveButton: Button
    private lateinit var dialog: Dialog

    private val PICK_IMAGE_REQUEST_CODE = 1
    private lateinit var profileImageUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        profileImageView = root.findViewById(R.id.profile_image)
        profileNameEditText = root.findViewById(R.id.profile_name)
        profileAddressEditText = root.findViewById(R.id.profile_address)
        profileSaveButton = root.findViewById(R.id.profile_save_btn)



        profileImageView.setOnClickListener { openImagePicker() }


            profileSaveButton.setOnClickListener {
                saveProfile()
            showProgressBar()}



        return root
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun saveProfile() {
        // Get the user's email
        val userEmail = FirebaseAuth.getInstance().currentUser!!.email!!

        val currentUser1 = FirebaseAuth.getInstance().currentUser

        // Get a reference to Firebase Storage
        val mAuth = FirebaseAuth.getInstance()
        // Get a reference to the profile image file in Firebase Storage
        val profileImageRef = FirebaseStorage.getInstance().getReference("user_business/"+mAuth.currentUser?.uid+".png")

        // Upload the profile image to Firebase Storage
        val uploadTask = profileImageRef.putFile(profileImageUri!!)
        val name = profileNameEditText.text.toString()
        val address = profileAddressEditText.text.toString()



        // Listen for the upload success event
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL of the uploaded image
            profileImageRef.downloadUrl.addOnSuccessListener { uri ->


                val ratings = 0.0

                val user = user_business_data_class(name, address, uri.toString(),userEmail, ratings)
                val db = FirebaseFirestore.getInstance()
                db.collection("user_business")
                    .document(userEmail)
                    .set(user)
                    .addOnSuccessListener {
                        val database = Firebase.database.reference
                        database.child("user_business").child(currentUser1!!.uid).setValue(user)
                            .addOnSuccessListener {
                                hideProgressBar()
                                Toast.makeText(activity, "Profile data saved to Realtime Database",
                                    Toast.LENGTH_SHORT).show()
                            }
                        hideProgressBar()
                        Toast.makeText(activity,"Business details  Saved SuccessFully ", Toast.LENGTH_SHORT)
                            .show()

                        profileNameEditText.text.clear()
                        profileAddressEditText.text.clear()
                        Log.d(TAG, "User data saved to Firestore")
                    }
                    .addOnFailureListener { e ->
                        hideProgressBar()
                        Log.e(TAG, "Error saving user data to Firestore", e)
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            profileImageUri = data.data!!
            Glide.with(this).load(profileImageUri).into(profileImageView)
        }
    }

    private fun showProgressBar() {
        dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }

}
