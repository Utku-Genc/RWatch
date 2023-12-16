package com.example.kotlindeneme4


import UserListAdapter
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kotlindeneme4.adapter.FavoriteAdapter
import com.example.kotlindeneme4.adapter.ProfileVideoAdapter
import com.example.kotlindeneme4.databinding.ActivityProfileBinding
import com.example.kotlindeneme4.model.UserModel
import com.example.kotlindeneme4.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding

    lateinit var favoriteAdapter: FavoriteAdapter
    lateinit var profileUserId: String
    lateinit var currentUserId: String
    lateinit var profileRoles: String

    lateinit var profileUserModel: UserModel
    lateinit var photoLauncher: ActivityResultLauncher<Intent>

    lateinit var adapter: ProfileVideoAdapter
    var userAdminAdapter: UserListAdapter? = null
    var profileVideoAdapter: ProfileVideoAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        profileUserId = intent.getStringExtra("profile_user_id")!!
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    //Fotoğraf Yükleme

                    uploadToFirestore(result.data?.data!!)
                }
            }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        if (profileUserId == currentUserId) {
            //Aynı kullanıcın profili
            binding.profileBtn.text = "Çıkış Yap"
            binding.profileBtn.setOnClickListener {
                logout()
            }

            //Foto
            binding.profilePic.setOnClickListener {
                checkPermissionAndPickPhoto()
            }
            binding.profilePicBtn.setOnClickListener {
                checkPermissionAndPickPhoto()
            }
            //Kullanıcı Adı
            binding.profileUsername.setOnClickListener {
                showUsernameEditDialog()
            }
            binding.profileUsernameBtn.setOnClickListener {
                showUsernameEditDialog()
            }

        } else {
            //Farklı kullanıcın profili
            binding.favoriText.text = "Favorileri"
            binding.profileBtn.text = "Takip Et"
            binding.profilePicBtn.visibility = View.INVISIBLE
            binding.profileUsernameBtn.visibility = View.INVISIBLE
            binding.profileBtn.setOnClickListener {
                followUnfollowUser()
            }
        }

        getProfileDataFromFirebase()
        setupRecyclerView()
    }

    fun followUnfollowUser() {
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener {
                val currentUserModel = it.toObject(UserModel::class.java)!!
                if (profileUserModel.followerList.contains(currentUserId)) {
                    //takip bırakma
                    profileUserModel.followerList.remove(currentUserId)
                    currentUserModel.followingList.remove(profileUserId)
                    binding.profileBtn.text = "Takip Et"
                } else {
                    //takip etme
                    profileUserModel.followerList.add(currentUserId)
                    currentUserModel.followingList.add(profileUserId)
                    binding.profileBtn.text = "Takibi Bırak"
                }
                updateUserData(profileUserModel)
                updateUserData(currentUserModel)
            }
    }

    fun updateUserData(model: UserModel) {
        Firebase.firestore.collection("users")
            .document(model.id)
            .set(model)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }

    }

    fun uploadToFirestore(photoUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        val photoRef = FirebaseStorage.getInstance()
            .reference
            .child("profilePic/" + currentUserId)
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    //video model store in firebase firestore
                    postToFirestore(downloadUrl.toString())
                }
            }

    }

    fun postToFirestore(url: String) {
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .update("profilePic", url)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    fun checkPermissionAndPickPhoto() {
        var readExternalPhoto: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readExternalPhoto = android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            readExternalPhoto = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                this,
                readExternalPhoto
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //we have permission
            openPhotoPicker()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalPhoto),
                100
            )
        }
    }


    // Kullanıcı adı düzenleme dialog'unu gösterme
    private fun showUsernameEditDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.rename_dialog, null)
        val editText = dialogView.findViewById<EditText>(R.id.nameEditText)

        dialogBuilder.setView(dialogView)

        dialogBuilder.setPositiveButton("Tamam") { _, _ ->
            val newUsername = editText.text.toString()
            updateUsername(newUsername)
        }

        dialogBuilder.setNegativeButton("İptal") { _, _ ->
            // İptal durumunda yapılacak işlemler
        }

        dialogBuilder.show()
    }


// ...

    // Kullanıcı adını güncelleme işlemi
    private fun updateUsername(newUsername: String) {
        val lowercaseUsername = newUsername.toLowerCase()

        // Kullanıcı adının benzersiz olup olmadığını kontrol et
        isUsernameUnique(lowercaseUsername) { isUnique ->
            if (isUnique) {
                // Benzersizse güncelleme işlemi yap
                Firebase.firestore.collection("users")
                    .document(currentUserId)
                    .update("username", lowercaseUsername)
                    .addOnSuccessListener {
                        getProfileDataFromFirebase()
                    }
            } else {
                // Benzersiz değilse kullanıcıya hata mesajı göster
                showToast("Bu kullanıcı adı zaten alınmış, lütfen başka bir tane seçin.")
            }
        }
    }

    private fun isUsernameUnique(username: String, callback: (Boolean) -> Unit) {
        Firebase.firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Eğer sorgu sonucunda hiç kullanıcı adı bulunmazsa, benzersizdir
                callback(querySnapshot.isEmpty)
            }
            .addOnFailureListener {
                // Hata durumunda işlemler
                callback(false)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openPhotoPicker() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun getProfileDataFromFirebase() {
        Firebase.firestore.collection("users")
            .document(profileUserId)
            .get()
            .addOnSuccessListener {
                profileUserModel = it.toObject(UserModel::class.java)!!
                setUi()
            }
    }


    fun setUi() {
        profileUserModel.apply {
            Glide.with(binding.profilePic).load(profilePic)
                .apply(RequestOptions().placeholder(R.drawable.icon_account_circle))
                .circleCrop()
                .into(binding.profilePic)
            binding.profileUsername.text = "@" + username
            if (profileUserModel.followerList.contains(currentUserId))
                binding.profileBtn.text = "Takibi Bırak"
            binding.progressBar.visibility = View.INVISIBLE
            binding.followingCount.text = followingList.size.toString()
            binding.followerCount.text = followerList.size.toString()
            Firebase.firestore.collection("videos")
                .whereEqualTo("uploaderId", profileUserId)
                .get().addOnSuccessListener {
                    binding.postCount.text = it.size().toString()
                }
            Firebase.firestore.collection("videos")
                .whereArrayContains("favoritedBy", profileUserId)
                .get().addOnSuccessListener {
                    val favoriCount = it.size().toString()
                    binding.favoriCount.text = favoriCount
                }

        }
    }

    fun setupRecyclerView() {
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereArrayContains("favoritedBy", profileUserId)
                    .orderBy("createdTime", Query.Direction.DESCENDING),
                VideoModel::class.java
            ).build()

        adapter = ProfileVideoAdapter(options)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
        binding.recyclerView.adapter = adapter


    }

    override fun onBackPressed() {
        // Diğer işlemleri buraya ekleyebilirsiniz.

        // Bir önceki Activity'e gitmek için
        super.onBackPressed()
        finish()
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