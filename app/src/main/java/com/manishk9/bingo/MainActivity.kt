package com.manishk9.bingo

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.manishk9.bingo.adapter.MainAdapter
import com.manishk9.bingo.databinding.ActivityMainBinding
import com.manishk9.bingo.databinding.ItemLetterBinding
import com.manishk9.bingo.model.Letter
import com.manishk9.bingo.model.Node

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var undoNode: Node? = null
    private var anim: ObjectAnimator? = null

    private val bingoManager by lazy {
        BingoManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter = MainAdapter()
        initializeRecylerView()
        binding.board.adapter = adapter
        adapter.setItemClickListener {
            with(bingoManager) {
                undoNode = it
                binding.undo.visibility = View.VISIBLE
                isSelectedValue(it.apply {
                    this.isVisited = true
                })
                adapter.notifyItemChanged(getValues().indexOf(it))
            }
        }
        adapter.submitList(bingoManager.getValues().toMutableList())

        bingoManager.getBingo().observe(this, Observer {
            if (it.isNotEmpty()) {
                resetNode()
                AddHeader(it.values.toList())
                adapter.setBoard(it)
                adapter.notifyDataSetChanged()
                if (it.size == 5) {
                    Toast.makeText(applicationContext, "GameOver Win", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.undo.setOnClickListener {
            undoNode?.let {
                bingoManager.undoNode(it)
                adapter.notifyItemChanged(bingoManager.getValues().indexOf(it))
                resetNode()
            }

        }

        bingoManager.getPostValue().observe(this, Observer {
            if (bingoManager.getBingo().value?.size == 5)
                return@Observer

            Toast.makeText(applicationContext, "PostValue", Toast.LENGTH_LONG).show()
        })

    }

    private fun initializeRecylerView() {
        with(binding.board) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@MainActivity, 5)

            val dividerItemDecoration =
                DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL)

            dividerItemDecoration.setDrawable(
                ContextCompat.getDrawable(this@MainActivity, R.drawable.divider_24dp)!!
            )
            val hor_dividerItemDecoration =
                DividerItemDecoration(this@MainActivity, DividerItemDecoration.HORIZONTAL)

            hor_dividerItemDecoration.setDrawable(
                ContextCompat.getDrawable(this@MainActivity, R.drawable.hor_divider_24dp)!!
            )

            addItemDecoration(dividerItemDecoration)
            addItemDecoration(hor_dividerItemDecoration)
        }
    }

    private fun AddHeader(letter: List<Letter>) {

        binding.bingo.removeAllViews()
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        for (item in letter) {
            val child = ItemLetterBinding.inflate(inflater, null)
            val lparams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            child.letter.text = item.letter
            child.letter.setTextColor(item.color)
            child.root.layoutParams = lparams
            binding.bingo.addView(child.root)
        }
    }

    private fun resetNode() {
        undoNode = null
        binding.undo.visibility = View.INVISIBLE
    }

    private fun blinkAnimation(view: View) {
        //starting cancel and end
        anim?.cancel()
        anim?.end()

        anim = ObjectAnimator.ofFloat(
            view,
            View.ALPHA,
            1.0f,
            0.25f
        )
        anim?.setDuration(3000)
        anim?.repeatMode = ObjectAnimator.REVERSE
        anim?.repeatCount = ObjectAnimator.INFINITE
        anim?.start()
    }


}