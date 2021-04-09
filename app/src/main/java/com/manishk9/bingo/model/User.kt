package com.manishk9.bingo.model

data class User(
    val userID: String,
    val username: String? = "Anonymous",
    val userEmail: String?,
    val userPic: String?,
    val createdOn: Long = System.currentTimeMillis()
) {
    constructor() : this("-1", "", "", "")
}