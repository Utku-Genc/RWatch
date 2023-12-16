package com.example.kotlindeneme4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindeneme4.adapter.VideoListAdapter
import com.example.kotlindeneme4.databinding.ActivitySingleVideoPlayerBinding
import com.example.kotlindeneme4.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class SingleVideoPlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivitySingleVideoPlayerBinding
    lateinit var videoId: String
    lateinit var adapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoId = intent.getStringExtra("videoId")!!
        setupViewPager()
    }

    fun setupViewPager() {
        val query = FirebaseFirestore.getInstance().collection("videos")
            .whereEqualTo("videoId", videoId)

        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(query, VideoModel::class.java)
            .build()

        // Burada context parametresini geçiyoruz
        adapter = VideoListAdapter(options)
        binding.viewPager.adapter = adapter
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
