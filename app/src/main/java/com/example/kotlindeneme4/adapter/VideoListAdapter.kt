package com.example.kotlindeneme4.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kotlindeneme4.MainActivity
import com.example.kotlindeneme4.ProfileActivity
import com.example.kotlindeneme4.R
import com.example.kotlindeneme4.databinding.VideoItemRowBinding
import com.example.kotlindeneme4.model.UserModel
import com.example.kotlindeneme4.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException


class VideoListAdapter(options: FirestoreRecyclerOptions<VideoModel>) :
    FirestoreRecyclerAdapter<VideoModel, VideoListAdapter.VideoViewHolder>(options) {


    private lateinit var currentUser: FirebaseUser
    private val firestore: FirebaseFirestore = Firebase.firestore

    inner class VideoViewHolder(private val binding: VideoItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var videoModel: VideoModel

        init {
            setupUI()
            setListeners()
        }

        private fun setupUI() {
            currentUser = FirebaseAuth.getInstance().currentUser!!
            binding.progressBar.visibility = View.VISIBLE
        }

        private fun setListeners() {
            binding.backBtn.setOnClickListener {
                val intent = Intent(binding.backBtn.context, MainActivity::class.java)
                binding.backBtn.context.startActivity(intent)
            }

            binding.heartBtn.setOnClickListener {
                toggleFavorite()
            }

            binding.userDetailLayout.setOnClickListener {
                navigateToProfile()
            }

            binding.videoView.apply {
                setOnClickListener {
                    toggleVideoPlayback()
                }
            }
        }

        private fun toggleFavorite() {
            val isFavorited = videoModel.favoritedBy.contains(currentUser.uid)
            firestore.collection("videos")
                .document(videoModel.videoId)
                .update(
                    "favoritedBy",
                    if (isFavorited) {
                        FieldValue.arrayRemove(currentUser.uid)
                    } else FieldValue.arrayUnion(currentUser.uid)
                )
                .addOnSuccessListener {
                    // Update successful
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        }

        private fun toggleVideoPlayback() {
            with(binding.videoView) {
                if (isPlaying) {
                    pause()
                    binding.pauseIcon.visibility = View.VISIBLE
                } else {
                    start()
                    binding.pauseIcon.visibility = View.GONE
                }
            }
        }

        private fun navigateToProfile() {
            val intent = Intent(binding.userDetailLayout.context, ProfileActivity::class.java)
            intent.putExtra("profile_user_id", videoModel.uploaderId)
            binding.userDetailLayout.context.startActivity(intent)
        }

        fun bindVideo(videoModel: VideoModel) {
            this.videoModel = videoModel

            firestore.collection("users")
                .document(videoModel.uploaderId)
                .get()
                .addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.apply {
                            usernameView.text = username
                            Glide.with(profileIcon).load(profilePic)
                                .circleCrop()
                                .apply(RequestOptions().placeholder(R.drawable.icon_account_circle))
                                .into(profileIcon)
                            favoriCounter.text = videoModel.favoritedBy.size.toString()
                            diziView.text = videoModel.diziad
                            bolumAdView.text = "(${videoModel.bolumad})"
                            sezonView.text = "${videoModel.sezon}. Sezon"
                            bolumView.text = "${videoModel.bolum}. Bölüm"
                            captionView.text = videoModel.title
                            yukleyenView.text = "Yükleyen"
                            progressBar.visibility = View.GONE

                            fetchImdbIdByTitle(videoModel.diziad)

                        }
                    }
                }

            // Update favorite icon
            binding.heartBtn.setImageResource(
                if (videoModel.favoritedBy.contains(currentUser.uid)) R.drawable.icon_heart_full
                else R.drawable.icon_heart
            )

            // Load video
            binding.videoView.apply {
                setVideoPath(videoModel.url)
                setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                    it.isLooping = true
                }
            }
        }


        private fun fetchImdbRatingByTitle(ttID: String) {
            val imdbPageUrl = "https://www.imdb.com/title/$ttID/"

            Thread {
                try {
                    val imdbDoc = Jsoup.connect(imdbPageUrl).get()
                    val ratingElement = imdbDoc.selectFirst(".sc-bde20123-1.cMEQkK")
                    val imdbRating = ratingElement?.text()

                    // Update UI with IMDb rating
                    binding.imbdPuan.post {
                        binding.imbdPuan.text = imdbRating
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }

        private fun fetchImdbIdByTitle(diziAd: String) {
            Thread {
                try {
                    val client = OkHttpClient()

                    val request = Request.Builder()
                        .url("https://imdb8.p.rapidapi.com/auto-complete?q=$diziAd")
                        .get()
                        .addHeader(
                            "X-RapidAPI-Key",
                            "19a371a361msh0fda596cce3ff18p1d18dejsn581870d7edc6"
                        )
                        .addHeader("X-RapidAPI-Host", "imdb8.p.rapidapi.com")
                        .build()

                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(response.body?.string())
                        val resultsArray = jsonResponse.getJSONArray("d")

                        if (resultsArray.length() > 0) {
                            val firstResult = resultsArray.getJSONObject(0)
                            val ttId = firstResult.optString("id", "")
                            if (ttId.isNotEmpty()) {
                                // fetchImdbRatingByTitle fonksiyonunu çağır
                                fetchImdbRatingByTitle(ttId)
                            }
                        } else {
                            println("ID bulunamadı.")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }


    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bindVideo(model)
    }
}
