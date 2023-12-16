package com.example.kotlindeneme4

import UserListAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlindeneme4.databinding.ActivityUserSettingsBinding
import com.example.kotlindeneme4.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserSettingsBinding
    private lateinit var userAdapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val firestore = FirebaseFirestore.getInstance()

        // Mevcut kullanıcının UID'sini al
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Firestore verilerini bir kere yükle
        firestore.collection("users")
            .orderBy("username") // "username" alanına göre alfabetik sıralama yapar
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = mutableListOf<UserModel>()
                var currentUser: UserModel? = null

                for (document in querySnapshot) {
                    val user = document.toObject(UserModel::class.java)

                    if (user.id == currentUserUid) {
                        currentUser = user
                    } else {
                        // Adminler hariç tüm kullanıcıları ekle
                        if (user.roles == "Admin" && user.id != currentUserUid) {
                            userList.add(user)
                        } else if (user.roles != "Admin") {
                            userList.add(user)
                        }
                    }
                }

                // Mevcut kullanıcıyı listenin başına ekle


                // Adminleri alfabetik sıraya göre listenin başına ekle
                val adminList = userList.filter { it.roles == "Admin" }.sortedBy { it.username }
                userList.removeAll(adminList)
                userList.addAll(0, adminList)

                currentUser?.let {
                    userList.add(0, it)
                }

                // UserListAdapter'ı başlatın ve RecyclerView'a bağlayın
                userAdapter = UserListAdapter(userList, currentUserUid)
                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = userAdapter
            }
            .addOnFailureListener { exception ->
                // Hata durumunda işlemler
            }



    }
}