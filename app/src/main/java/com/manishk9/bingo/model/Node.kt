package com.manishk9.bingo.model

data class Node(
    val id: Int,
    val value: String,
    val row: Int,
    val column: Int,
    val isCenterdiagonal: Boolean = false,
    val isDiagonal: Boolean = false,
    var isVisited: Boolean = false,
    var tag: MutableList<String> = mutableListOf(),
    var selectedBy: NodeSelection = NodeSelection.Nothing
) {
    override fun equals(other: Any?): Boolean {
        when (other) {
            is Node -> {
                return this.id == other.id
            }
            else -> return false
        }
    }

    constructor(id: Int) : this(id, "", -1, -1)
}

enum class NodeSelection {
    ME, Opponent, Nothing
}
