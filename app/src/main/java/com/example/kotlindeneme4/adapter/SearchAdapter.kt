package com.example.kotlindeneme4.adapter

import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlindeneme4.R
import com.example.kotlindeneme4.SingleVideoPlayerActivity
import com.example.kotlindeneme4.model.UserModel
import com.example.kotlindeneme4.model.VideoModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class SearchAdapter(
    private val videoList: List<VideoModel>,
    private val onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<SearchAdapter.VideoViewHolder>() {

    interface OnItemClickListener {

        fun onItemClick(videoModel: VideoModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoModel = videoList[position]
        holder.bind(videoModel)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diziIsimView: TextView = itemView.findViewById(R.id.dizi_isim_view)
        private val listBorder: RelativeLayout = itemView.findViewById(R.id.list_border)
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnail_image_view)
        private val diziSezonView: TextView = itemView.findViewById(R.id.dizi_sezon_view)
        private val diziBolumView: TextView = itemView.findViewById(R.id.dizi_bolum_view)
        private val diziAcKlamaView: TextView = itemView.findViewById(R.id.dizi_acıklama_view)

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
                            thumbnailImageView.post {
                                Glide.with(thumbnailImageView)
                                    .load(it)
                                    .into(thumbnailImageView)
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
                        diziIsimView.text = videoModel.diziad
                        listBorder.setOnClickListener {
                            val intent = Intent(
                                thumbnailImageView.context,
                                SingleVideoPlayerActivity::class.java
                            )
                            intent.putExtra("videoId", videoModel.videoId)
                            thumbnailImageView.context.startActivity(intent)
                        }

                        diziIsimView.text = videoModel.diziad
                        diziSezonView.text = videoModel.sezon + ".Sezon"
                        diziBolumView.text = videoModel.bolum + ". Bölüm"
                        diziAcKlamaView.text = videoModel.title
                    }
                }
        }
    }
}
