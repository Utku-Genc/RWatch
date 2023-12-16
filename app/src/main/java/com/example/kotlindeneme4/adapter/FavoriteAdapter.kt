package com.example.kotlindeneme4.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlindeneme4.R
import com.example.kotlindeneme4.model.UserModel
import com.example.kotlindeneme4.model.VideoModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FavoriteAdapter(private val videoList: List<VideoModel>, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<FavoriteAdapter.VideoViewHolder>() {

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
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val userModel = documentSnapshot.toObject(UserModel::class.java)

                    userModel?.let { user ->
                        // Kullanıcı bilgilerini kullanarak gerekli düzenlemeleri yap
                        diziIsimView.text = videoModel.diziad
                        listBorder.setOnClickListener {
                            onItemClickListener.onItemClick(videoModel)
                        }

                        // Glide kütüphanesi ile resmi yükle
                        Glide.with(thumbnailImageView)
                            .load(videoModel.url)
                            .into(thumbnailImageView)

                        thumbnailImageView.setOnClickListener {
                            onItemClickListener.onItemClick(videoModel)
                        }

                        // Diğer video bilgilerini göster
                        diziSezonView.text = "${videoModel.sezon}. Sezon"
                        diziBolumView.text = "${videoModel.bolum}. Bölüm"
                        diziAcKlamaView.text = videoModel.title
                    }
                }
                .addOnFailureListener { exception ->
                    // Hata durumunda yapılacak işlemler
                    Log.e("Firestore", "Error getting user document", exception)
                }
        }


    }
}
