package com.manishk9.bingo

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import com.manishk9.bingo.model.Letter
import com.manishk9.bingo.model.Node
import kotlin.random.Random

class BingoManager {

    private var allValues: ArrayList<Node> = arrayListOf()
    private val bingo = MutableLiveData<MutableMap<String, Letter>>()
    private val postValue: MutableLiveData<String> = MutableLiveData()

    init {
        bingo.value = mutableMapOf()
        generateBoard()
    }

    private fun generateBoard() {
        allValues = fillvalue(generateRandomNumber())
    }

    fun getBingo(): MutableLiveData<MutableMap<String, Letter>> {
        return bingo
    }

    fun getPostValue(): MutableLiveData<String> {
        return postValue
    }

    fun getValues(): ArrayList<Node> {
        return allValues
    }

    private fun generateRandomNumber(
        size: Int = 25,
        startRange: Int = 1,
        EndRange: Int = 26
    ): ArrayList<Int> {
        val list = arrayListOf<Int>()
        do {
            val value = Random.nextInt(startRange, EndRange)
            if (!list.contains(value))
                list.add(value)
        } while (list.size < size)
        return list
    }

    private fun fillvalue(allEntries: ArrayList<Int>): ArrayList<Node> {

        val nodelist: ArrayList<Node> = arrayListOf()
        var lastIndex = 5
        for (i in 0..20 step 5) {
            val slice = allEntries.slice(IntRange(i, (i + 5) - 1))
            nodelist.addAll(getNodelist(slice, i / 5, --lastIndex))
        }
        return nodelist
    }

    private fun getNodelist(elements: List<Int>, column: Int, lastIndex: Int): ArrayList<Node> {
        val rowelements: ArrayList<Node> = arrayListOf()
        for (i in 0..elements.size - 1) {
            val childNode =
                Node(
                    elements.get(i),
                    value = elements.get(i).toString(),
                    row = i,
                    column = column,
                    isCenterdiagonal = isCenter(i, column),
                    isDiagonal = (i == column || i == lastIndex)
                )
            rowelements.add(childNode)
        }
        return rowelements
    }

    private fun isCenter(row: Int, column: Int): Boolean {
        return row == 2 && column == 2
    }

    fun isSelectedValue(value: Node) {
        isallRowVisited(value.row)
        isallColumnVisited(value.column)
        if (value.isCenterdiagonal && value.isDiagonal) {
            isallDiagonalVisited(4, 4)
        } else if (value.isDiagonal) {
            if (value.row == value.column)
                isallDiagonalVisited()
            else
                isallDiagonalVisited(4, 4)
        }
        postValue.value = value.value
    }

    private fun isallRowVisited(row: Int) {
        for (i in row..20 + row step 5) {
            if (!allValues.get(i).isVisited)
                return
        }
        addBingo("Row$row", row, row + 20, 5)

    }

    private fun isallColumnVisited(column: Int) {
        val startIndex = column * 5
        for (i in startIndex..startIndex + 4) {
            if (!allValues[i].isVisited)
                return
        }
        addBingo("Column$column", startIndex, startIndex + 4)
    }

    private fun isallDiagonalVisited(number: Int = 0, steps: Int = 6) {
        for (i in number..25 - number step steps) {
            if (!allValues[i].isVisited)
                return
        }
        addBingo("Diagonal$number", number, 25 - number, steps)
    }

    private fun addBingo(key: String, startRange: Int, EndRange: Int, steps: Int = 1) {
        val temp = bingo.value ?: HashMap()

        //if already contain or if gameover
        if (temp.contains(key) || temp.size == 5)
            return

        when (temp.size) {
            0 -> temp[key] = Letter("B", Color.parseColor("#b7acd8"))
            1 -> temp[key] = Letter("I", Color.parseColor("#FFD700"))
            2 -> temp[key] = Letter("N", Color.parseColor("#f6f7fc"))
            3 -> temp[key] = Letter("G", Color.parseColor("#fbbee0"))
            4 -> temp[key] = Letter("O", Color.parseColor("#cfffe5"))
        }

        addTag(key, startRange, EndRange, steps)
        bingo.value = temp
    }

    private fun addTag(key: String, startRange: Int, EndRange: Int, steps: Int = 1) {
        for (i in startRange..EndRange step steps) {
            allValues[i].tag.add(key)
        }
    }

    fun undoNode(item: Node) {
        val index = allValues.indexOf(item)
        val child = allValues.get(index)
        child.isVisited = false
    }


}