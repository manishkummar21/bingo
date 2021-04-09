package com.manishk9.bingo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.toObject
import com.manishk9.bingo.R
import com.manishk9.bingo.RoomType
import com.manishk9.bingo.databinding.FragmentRoomdetailBinding
import com.manishk9.bingo.model.Room
import com.manishk9.bingo.model.RoomStatus
import com.manishk9.bingo.model.User
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RoomDetail : Fragment() {

    private var _binding: FragmentRoomdetailBinding? = null
    private val binding get() = _binding!!

    val args: RoomDetailArgs by navArgs()
    private var registration: ListenerRegistration? = null

    @Inject
    lateinit var db: FirebaseFirestore

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentRoomdetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerRoom()
        binding.roomCode.text = args.roomId
        auth.currentUser?.let {
            fetchUserDetails(it.uid)
        }

        binding.start.setOnClickListener {
            val dbRef = db.collection("rooms").document(args.roomId)
            dbRef.update("roomStatus", RoomStatus.Started).addOnCompleteListener { }
        }
    }

    private fun registerRoom() {

        val docRef = db.collection("rooms").document(args.roomId)
        registration = docRef.addSnapshotListener() { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val roomDetials = snapshot.toObject<Room>()
                if (roomDetials?.roomStatus == RoomStatus.Started) {
                    registration?.remove()
                    findNavController().navigate(
                        RoomDetailDirections.actionRoomDetailToBingoBoardFragment(
                            args.roomId,
                            args.roomType,
                            binding.opponentUsername.tag as String,
                            binding.currentUsername.tag as String,
                            binding.opponentUsername.text.toString()
                        )
                    )
                    return@addSnapshotListener
                }
                if (roomDetials?.members?.size == 2) {
                    roomDetials.members.let {
                        val opponetID = it.filter { s -> !s.equals(auth.currentUser?.uid) }
                        fetchUserDetails(opponetID.get(0), true)
                    }
                }
            }
        }
    }

    private fun fetchUserDetails(userID: String, isOpponent: Boolean = false) {
        val docRef = db.collection("users").document(userID)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject<User>()
            user?.let {
                if (!isOpponent)
                    setCurrentUserDetail(it)
                else {
                    setOppoUserDetail(it)
                    showstartButton()
                    showfooter()
                }
            }

        }
    }

    private fun showfooter() {
        binding.footer.text =
            if (RoomType.Create == args.roomType) resources.getString(R.string.start_by_you) else String.format(
                Locale.getDefault(),
                getString(R.string.start_by_owner),
                binding.opponentUsername.text
            )
    }


    private fun setCurrentUserDetail(user: User) {
        setImage(user.userPic, binding.currentUserPic)
        binding.currentUsername.tag = user.userID
        binding.currentUsername.text = user.username ?: user.userEmail ?: "Guest"
    }

    private fun setOppoUserDetail(user: User) {
        setImage(user.userPic, binding.opponentUserPic)
        binding.opponentUsername.tag = user.userID
        binding.opponentUsername.text = user.username ?: user.userEmail ?: "Guest"
    }

    private fun setImage(url: String?, view: AppCompatImageView) {
        url?.let {
            val large_url = url.plus("?type=large")
            Glide.with(requireContext())
                .load(large_url)
                .transform(
                    CenterCrop(),
                    RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius_8dp))
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view)
        }
    }

    private fun showstartButton() {
        if (args.roomType == RoomType.Create)
            binding.start.visibility = View.VISIBLE
    }
}