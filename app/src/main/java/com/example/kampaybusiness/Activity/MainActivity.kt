package com.example.kampaybusiness

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView

import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kampaybusiness.databinding.ActivityMainBinding
import com.example.kampaybusiness.fragments.HomeFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.jakewharton.threetenabp.AndroidThreeTen
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialog: Dialog
    private lateinit var mDatabase: DatabaseReference
    private lateinit var user1: user_business_data_class
    private lateinit var uid: String
    private lateinit var sideMenu: NavigationView
    private lateinit var menu_btn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AndroidThreeTen.init(this)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser!!.uid
        mDatabase = FirebaseDatabase.getInstance().getReference("user_business")
        showFragment(HomeFragment())

        if (uid.isNotEmpty()) {
            showProgressBar()
            getUserData()
        }

        sideMenu = findViewById(R.id.navigation_view)
        sideMenu.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_signout -> {
                    Firebase.auth.signOut()
                    finish()
                    startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                    true
                }
                R.id.Profile -> showFragment(ProfileFragment())
                R.id.Home -> showFragment(HomeFragment())

            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        menu_btn = findViewById(R.id.menuButton)
        menu_btn.setOnClickListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawerLayout.openDrawer(GravityCompat.START)
            window.statusBarColor = this.resources.getColor(R.color.white)
        }

    }

    private fun getUserData() {
        mDatabase.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    hideProgressBar()
                    sideMenu.visibility = View.VISIBLE
                    menu_btn.visibility = View.VISIBLE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    user1 = snapshot.getValue(user_business_data_class::class.java)!!

                    val headerView =
                        findViewById<NavigationView>(R.id.navigation_view).getHeaderView(0)

                    val userNameTextView = headerView.findViewById<TextView>(R.id.user_name)
                    val userEmailTextView = headerView.findViewById<TextView>(R.id.user_email)
                    val ratingsTextView = headerView.findViewById<TextView>(R.id.ratings1)
                    // Get the ImageView that displays the star
                    val starImageView = findViewById<ImageView>(R.id.rating_star)


                    val profileImageUrlSnapshot = snapshot.child("businessLogo")

                    if (profileImageUrlSnapshot.exists()) {
                        val profileImageUrl = profileImageUrlSnapshot.value.toString()
                        loadImage(profileImageUrl)
                    } else {
                        Toast.makeText(
                            this@MainActivity, "Error getting profile photo",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    userNameTextView.text = user1?.businessName
                    userEmailTextView.text = user1?.email

                    val ratings = user1?.ratings?.toFloat() ?: 0f

                    val formattedRatings = if (ratings > 5.0) "5.0"
                    else ratings.toString().toDouble()


                    if (ratings >= 4.50) {
                        ratingsTextView.setTextColor(Color.parseColor("#318F2F")) // light green
                    } else if (ratings >= 4.00 && ratings < 4.50) {
                        ratingsTextView.setTextColor(Color.parseColor("#3CC93A")) // green
                    } else if (ratings >= 3.50 && ratings < 4.00) {
                        ratingsTextView.setTextColor(Color.parseColor("#B1AB16")) // yellow
                    } else if (ratings >= 3.00 && ratings < 3.50) {
                        ratingsTextView.setTextColor(Color.parseColor("#E5DE21")) // light yellow
                    } else if (ratings >= 2.50 && ratings < 3.00) {
                        ratingsTextView.setTextColor(Color.parseColor("#B18616")) // orange
                    } else if (ratings >= 2.00 && ratings < 2.50) {
                        ratingsTextView.setTextColor(Color.parseColor("#FFC225")) // light orange
                    } else {
                        ratingsTextView.setTextColor(Color.parseColor("#FF3F25")) // red
                    }

                    ratingsTextView.text = formattedRatings.toString() // set ratings text

                    val color = when {
                        ratings >= 4.5 -> R.color.dark_green

                        ratings >= 4.0 -> R.color.light_green
                        ratings >= 3.5 -> R.color.dark_yellow
                        ratings >= 3.0 -> R.color.light_yellow
                        ratings >= 2.5 -> R.color.dark_orange
                        ratings >= 2.0 -> R.color.light_orange
                        else -> R.color.dark_red
                    }
                    starImageView.setColorFilter(
                        ContextCompat.getColor(this@MainActivity, color), PorterDuff.Mode.SRC_IN
                    )


                } else {
                    hideProgressBar()
                    showFragment(ProfileFragment())
                    sideMenu.visibility = View.GONE
                    menu_btn.visibility = View.GONE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    Toast.makeText(
                        this@MainActivity, "User data not found, Please Update your Profile",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    onBackPressed()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressBar()
                showFragment(ProfileFragment())
                Toast.makeText(this@MainActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun loadImage(profileImageUrl: String) {
        val headerView = findViewById<NavigationView>(R.id.navigation_view).getHeaderView(0)
        val userProfileImageView =
            headerView.findViewById<ImageView>(R.id.profile_pic_user_sidemenu)

        // use Glide library to load the image into the ImageView
        Glide.with(this)
            .load(profileImageUrl)
            .placeholder(R.drawable.profile_default_photo) // You can also set a placeholder image here
            .error(R.drawable.error_image) // You can set an error image here in case the actual image fails to load
            .into(userProfileImageView)

    }


    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        // Do nothing to disable the back button
    }


    private fun showProgressBar() {
        dialog = Dialog(this@MainActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar() {
        dialog.dismiss()
    }
}