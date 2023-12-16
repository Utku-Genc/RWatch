package com.example.kotlindeneme4.model

data class UserModel(
    var id : String = "",
    var email : String = "",
    var username : String = "",
    var profilePic : String = "",
    var roles : String = "User",
    var block : Boolean = false,
    var followerList: MutableList<String> = mutableListOf(),
    var followingList: MutableList<String> = mutableListOf(),
    var favList: MutableList<String> = mutableListOf()
)
