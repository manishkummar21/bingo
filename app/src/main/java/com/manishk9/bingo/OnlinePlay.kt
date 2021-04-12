package com.manishk9.bingo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.manishk9.bingo.fragment.BingoBoardFragmentArgs

class OnlinePlay : BaseFragment(){

    private val args: BingoBoardFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize(args.toBundle())
    }

    override fun postData() {

    }

    override fun onGameFinished() {
    }
}