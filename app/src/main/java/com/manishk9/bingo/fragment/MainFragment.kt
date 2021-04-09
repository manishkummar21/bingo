package com.manishk9.bingo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.resources.MaterialResources.getDimensionPixelSize
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.manishk9.bingo.R
import com.manishk9.bingo.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth.currentUser?.let {
            Glide.with(this)
                .load(it.photoUrl)
                .transform(
                    CenterCrop(),
                    RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius))
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.userProfilePic)

            binding.username.text = String.format(
                Locale.getDefault(),
                resources.getString(R.string.hello_username),
                it.displayName
            )
        }
        binding.playFriend.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createJoinRoom)
        }

        binding.playOffline.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Not Yet Implemented", Toast.LENGTH_LONG)
                .show()
        }
    }
}