package com.example.kotlindeneme4.model

import com.google.firebase.Timestamp

data class VideoModel(
    var videoId: String = "",
    var title: String = "",
    var url: String = "",
    var uploaderId: String = "",
    var createdTime: Timestamp = Timestamp.now(),
    var diziad: String = "",
    var bolumad: String = "",
    var sezon: String = "",
    var bolum: String = "",
    var favorite: Int = 0,
    var favoritedBy: List<String> = mutableListOf() // New property to store user IDs who have favorited the video
)
