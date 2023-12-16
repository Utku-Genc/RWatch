package com.example.kotlindeneme4.adapter

import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlindeneme4.SingleVideoPlayerActivity
import com.example.kotlindeneme4.databinding.ProfileVideoItemRowBinding
import com.example.kotlindeneme4.model.UserModel
import com.example.kotlindeneme4.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class ProfileVideoAdapter(options: FirestoreRecyclerOptions<VideoModel>) :
    FirestoreRecyclerAdapter<VideoModel, ProfileVideoAdapter.VideoViewHolder>(options) {

    inner class VideoViewHolder(private val binding: ProfileVideoItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(videoModel: VideoModel) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://imdb8.p.rapidapi.com/auto-complete?q=${videoModel.diziad}")
                .get()
                .addHeader("X-RapidAPI-Key", "19a371a361msh0fda596cce3ff18p1d18dejsn581870d7edc6")
                .addHeader("X-RapidAPI-Host", "imdb8.p.rapidapi.com")
                .build()

            AsyncTask.execute {
                try {
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val jsonResponse = response.body?.string()
                        val jsonObject = JSONObject(jsonResponse)

                        val imageUrl =
                            jsonObject.optJSONArray("d")?.optJSONObject(0)?.optJSONObject("i")
                                ?.optString("imageUrl")

                        imageUrl?.let {
                            binding.thumbnailImageView.post {
                                Glide.with(binding.thumbnailImageView)
                                    .load(it)
                                    .into(binding.thumbnailImageView)
                            }
                        }
                    } else {
                        // Handle API request failure
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            // Atama kodları
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get().addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.diziIsimView.text = videoModel.diziad
                        binding.listBorder.setOnClickListener {
                            val intent = Intent(
                                binding.thumbnailImageView.context,
                                SingleVideoPlayerActivity::class.java
                            )
                            intent.putExtra("videoId", videoModel.videoId)
                            binding.thumbnailImageView.context.startActivity(intent)
                        }

                        binding.diziIsimView.text = videoModel.diziad
                        binding.diziSezonView.text = videoModel.sezon + ".Sezon"
                        binding.diziBolumView.text = videoModel.bolum + ". Bölüm"
                        binding.diziAcKlamaView.text = videoModel.title
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding =
            ProfileVideoItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bind(model)
    }


}


