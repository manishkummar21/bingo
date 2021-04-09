package com.manishk9.bingo.model

data class Room(
    val id: String,
    val members: ArrayList<String> = arrayListOf(),
    val roomStatus: RoomStatus = RoomStatus.Created,
    val createTime: Long = System.currentTimeMillis(),
    val quitBy: String? = null,
    val winBy: String? = null
) {
    constructor() : this("-1", arrayListOf(), RoomStatus.ERROR)
}

enum class RoomStatus {
    Created, Started, Finished, QUIT, ERROR
}