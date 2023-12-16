package com.example.kotlindeneme4

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindeneme4.adapter.SearchAdapter
import com.example.kotlindeneme4.model.VideoModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchActivity : AppCompatActivity(), SearchAdapter.OnItemClickListener {

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var videoList: MutableList<VideoModel>
    private lateinit var filteredList: MutableList<VideoModel>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        videoList = mutableListOf()
        filteredList = mutableListOf()
        firestore = FirebaseFirestore.getInstance()


        val backButton: ImageButton = findViewById(R.id.back_btn)

        backButton.setOnClickListener {
            onBackPressed()
        }
        // RecyclerView ve SearchAdapter'ı oluştur
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        searchAdapter = SearchAdapter(videoList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = searchAdapter

        // Arama işlevselliği için EditText'i dinle
        val searchEditText: EditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Firestore'dan verileri al
        fetchVideoData()
    }

    private fun filterList(query: String) {
        filteredList.clear()

        for (video in videoList) {
            if (video.title.contains(query, ignoreCase = true) ||
                video.diziad.contains(query, ignoreCase = true) ||
                video.bolumad.contains(query, ignoreCase = true) ||
                video.uploaderId.contains(query, ignoreCase = true)
            ) {
                filteredList.add(video)
            }
        }

        searchAdapter = SearchAdapter(filteredList, this)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter = searchAdapter
    }

    private fun fetchVideoData() {
        firestore.collection("videos")
            .orderBy("createdTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                videoList.clear()
                for (document in result) {
                    val video = document.toObject(VideoModel::class.java)
                    videoList.add(video)
                }
                searchAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Hata durumunda yapılacak işlemler
            }
    }

    override fun onItemClick(videoModel: VideoModel) {
        // Tıklandığında yapılacak işlemler
        // Örneğin, seçilen videoyu başka bir ekranda oynatma
    }
}
