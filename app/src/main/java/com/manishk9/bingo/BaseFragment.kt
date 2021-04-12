package com.manishk9.bingo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import com.manishk9.bingo.adapter.MainAdapter
import com.manishk9.bingo.databinding.FragmentBingoboardBinding

abstract class BaseFragment : Fragment() {

    private var _binding: FragmentBingoboardBinding? = null
    private val binding get() = _binding!!

    private var adapter: MainAdapter? = null

    private var data = Bundle()

    abstract fun postData()

    abstract fun onGameFinished()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding == null)
            _binding = FragmentBingoboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun initialize(data: Bundle) {
        this.data = data
    }

    fun getBindingBoard(): FragmentBingoboardBinding {
        return binding
    }
}