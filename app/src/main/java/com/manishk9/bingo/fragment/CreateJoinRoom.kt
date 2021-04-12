package com.manishk9.bingo.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.manishk9.bingo.RoomType
import com.manishk9.bingo.Utils
import com.manishk9.bingo.databinding.FragmentCreatejoinBinding
import com.manishk9.bingo.model.Room
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class CreateJoinRoom : Fragment() {
    private var _binding: FragmentCreatejoinBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var db: FirebaseFirestore

    private val progressDialog: Dialog by lazy {
        Utils.progressDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentCreatejoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createRoom.setOnClickListener {
            progressDialog.show()

            auth.currentUser?.let {
                val room =
                    Room(generateSixDigitRandomNumber(), arrayListOf(it.uid), createBy = it.uid)
                db.collection("rooms").document(room.id).set(room)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        findNavController().navigate(
                            CreateJoinRoomDirections.actionCreateJoinRoomToRoomDetail(
                                room.id,
                                RoomType.Create
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.w("", "Error writing document", e)
                        progressDialog.dismiss()
                    }
            } ?: progressDialog.dismiss()

        }

        binding.join.setOnClickListener {
            progressDialog.show()
            auth.currentUser?.let {
                if (binding.inputRoom.length() != 0) {
                    val roomID = binding.inputRoom.text.toString()
                    val dbRef = db.collection("rooms").document(roomID)
                    dbRef.update("members", FieldValue.arrayUnion(it.uid)).addOnSuccessListener {
                        binding.inputRoom.setText("")
                        progressDialog.dismiss()
                        findNavController().navigate(
                            CreateJoinRoomDirections.actionCreateJoinRoomToRoomDetail(
                                roomID,
                                RoomType.JOIN
                            )
                        )
                    }

                } else {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Enter a Code", Toast.LENGTH_LONG).show()
                }
            } ?: progressDialog.dismiss()

        }


    }

    private fun generateSixDigitRandomNumber(): String {
        val rnd = Random
        val number: Int = rnd.nextInt(999999) + rnd.nextInt(99999) + rnd.nextInt(9999)
        return String.format("%06d", number)
    }
}