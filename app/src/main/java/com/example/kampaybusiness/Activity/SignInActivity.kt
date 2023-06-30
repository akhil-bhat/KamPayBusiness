package com.example.kampaybusiness

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.example.kampaybusiness.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)




        mAuth = FirebaseAuth.getInstance()
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            showProgressBar()


            if (email.isNotEmpty() && pass.isNotEmpty()) {

                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        hideProgressBar()
                        val intent =
                            Intent(this@SignInActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        hideProgressBar()
                        Toast.makeText(
                            this@SignInActivity,
                            it.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            } else {
                hideProgressBar()
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }

        val emailAddress = "akhilbhatnagar2001@gmail.com"
        val emailSubject = "Regarding Password Reset"
        val emailBody = "Type your email to reset password [' '] , you'll receive an mail regarding reset password"
        val forgot_pass = binding.forgotPassTV

        forgot_pass.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
                setPackage("com.google.android.gm")
            }


            try {
                //start email intent
                startActivity(Intent.createChooser(intent, "Choose Email Client..."))
            }
            catch (e: Exception){

                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }


    }
    override fun onStart() {

        val currentUser = mAuth.currentUser

        if (mAuth.currentUser != null) {

            updateUI1(currentUser)
        }
        super.onStart()
    }

    private fun updateUI1(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            // User is signed in, update UI with their information
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    }


    private fun showProgressBar() {
        dialog = Dialog(this@SignInActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }
}