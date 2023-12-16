package com.example.kotlindeneme4

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlindeneme4.adapter.ProfileVideoAdapter
import com.example.kotlindeneme4.databinding.ActivityMainBinding
import com.example.kotlindeneme4.model.UserModel
import com.example.kotlindeneme4.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var profileUserModel: UserModel
    lateinit var photoLauncher: ActivityResultLauncher<Intent>
    lateinit var currentUserId: String

    lateinit var adapter: ProfileVideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!


        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val roles = documentSnapshot.getString("roles")

                    // roles değeri admin ise user_admin_btn'yi göster
                    if (roles == "Admin") {
                        binding.bottomNavBar.visibility = View.GONE
                        binding.adminBottomNavBar.visibility = View.VISIBLE
                    }
                    else{
                        binding.bottomNavBar.visibility = View.VISIBLE
                    }
                }
            }
        binding.adminBottomNavBar.setOnItemSelectedListener {menuItem->
            when(menuItem.itemId){
                R.id.admin_bottom_menu_home->{
                }

                R.id.bottom_menu_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                }

                R.id.admin_bottom_menu_upload-> {
                    startActivity(Intent(this, VideoUploadActivity::class.java))
                }

                R.id.admin_bottom_menu_users->{
                    startActivity(Intent(this, UserSettingsActivity::class.java))
                }

                R.id.admin_bottom_menu_profile->{
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid)
                    startActivity(intent)
                }
            }
            false
        }

        binding.bottomNavBar.setOnItemSelectedListener {menuItem->
            when(menuItem.itemId){
                R.id.bottom_menu_home->{
                }

                R.id.bottom_menu_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                }

                R.id.bottom_menu_profile->{
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid)
                    startActivity(intent)
                }
            }
            false
        }

        setupRecyclerView()
    }



    fun setupRecyclerView() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .orderBy("createdTime", Query.Direction.DESCENDING),
                VideoModel::class.java
            ).build()
        adapter = ProfileVideoAdapter(options)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
        binding.recyclerView.adapter = adapter
    }



    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

}