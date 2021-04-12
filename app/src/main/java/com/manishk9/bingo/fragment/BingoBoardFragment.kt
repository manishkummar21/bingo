package com.manishk9.bingo.fragment

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.manishk9.bingo.BingoManager
import com.manishk9.bingo.R
import com.manishk9.bingo.RoomType
import com.manishk9.bingo.adapter.MainAdapter
import com.manishk9.bingo.databinding.FragmentBingoboardBinding
import com.manishk9.bingo.databinding.ItemLetterBinding
import com.manishk9.bingo.model.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BingoBoardFragment : Fragment() {
    private var _binding: FragmentBingoboardBinding? = null
    private val binding get() = _binding!!

    private var undoNode: Node? = null

    private val bingoManager by lazy {
        BingoManager()
    }

    private val args: BingoBoardFragmentArgs by navArgs()

    @Inject
    lateinit var db: FirebaseFirestore

    private var registration: ArrayList<ListenerRegistration> = arrayListOf()

    private var postData: PostData = PostData()

    private var adapter: MainAdapter? = null

    private var anim: ObjectAnimator? = null

    private var opponentValue: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentBingoboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.opponentUsername.text = args.opponentName
        adapter = MainAdapter()
        registerListener()
        initializeRecylerView()
        binding.board.adapter = adapter
        adapter?.submitList(bingoManager.getValues().toMutableList())

        adapter?.setItemClickListener {
            enable_disable(false)
            updateBoard(it, NodeSelection.ME)
        }

        bingoManager.getBingo().observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                resetNode()
                AddHeader(it.values.toList())
                adapter?.setBoard(it)
                adapter?.notifyDataSetChanged()
                if (it.size == 5) {
                    UpdateGameStatus()
                }
            }
        })

        binding.undo.setOnClickListener {
            undoNode?.let {
                bingoManager.undoNode(it)
                adapter?.notifyItemChanged(bingoManager.getValues().indexOf(it))
                resetNode()
            }

        }

        bingoManager.getPostValue().observe(viewLifecycleOwner, Observer {
            if (bingoManager.getBingo().value?.size == 5 || opponentValue == it.toInt())
                return@Observer
            postData(it)

        })

        binding.overlay.setOnClickListener { }


        binding.userboard.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.currentUsername.id)
                blinkAnimation(binding.currentUsername)
            else
                blinkAnimation(binding.opponentUsername)
        }

        showTag()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val builder = AlertDialog.Builder(requireContext())
            //set title for alert dialog
            builder.setTitle("Exit")
            //set message for alert dialog
            builder.setMessage("Are u sure you want to quit the game?")

            builder.setPositiveButton("Yes") { _, _ ->
                val dbRef = db.collection("rooms").document(args.roomID)
                dbRef.update(
                    "roomStatus",
                    RoomStatus.QUIT,
                    "quitBy",
                    args.currentID,
                    "winBy",
                    args.opponentID
                ).addOnSuccessListener {
                }
            }
            //performing negative action
            builder.setNegativeButton("No") { _, _ ->

            }

            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    private fun updateBoard(selectednode: Node, selection: NodeSelection) {
        with(bingoManager) {
            isSelectedValue(selectednode.apply {
                this.isVisited = true
                this.selectedBy = selection
            })
            adapter?.notifyItemChanged(getValues().indexOf(selectednode))
        }
    }

    private fun UpdateGameStatus() {
        //update the ROOM Node
        val dbRef = db.collection("rooms").document(args.roomID)
        dbRef.update("roomStatus", RoomStatus.Finished, "winBy", args.currentID)
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show()
            }
    }

    private fun showTag() {
        if (args.roomType == RoomType.Create) {
            enable_disable(true)
            Snackbar.make(binding.root, "You are breaking", Snackbar.LENGTH_LONG).show()
        } else {
            enable_disable(false)
            Snackbar.make(binding.root, "Opponent will break", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initializeRecylerView() {
        with(binding.board) {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 5)

            val dividerItemDecoration =
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

            dividerItemDecoration.setDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.divider_24dp)!!
            )
            val hor_dividerItemDecoration =
                DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)

            hor_dividerItemDecoration.setDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.hor_divider_24dp)!!
            )

            addItemDecoration(dividerItemDecoration)
            addItemDecoration(hor_dividerItemDecoration)
        }
    }

    private fun AddHeader(letter: List<Letter>) {

        binding.bingo.removeAllViews()
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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

    private fun enable_disable(flag: Boolean = true) {
        if (flag) {
            binding.overlay.visibility = View.GONE
            binding.currentUsername.isChecked = true
        } else {
            binding.overlay.visibility = View.VISIBLE
            binding.opponentUsername.isChecked = true
        }
    }

    private fun postData(value: String) {
        val docRef =
            db.collection("rooms").document(args.roomID).collection("channel${args.opponentID}")
                .document(args.opponentID)
        postData.number = value
        docRef.set(postData).addOnFailureListener {
            Toast.makeText(requireContext(), "Something went Wrong", Toast.LENGTH_LONG).show()
        }
    }

    private fun registerListener() {
        val docRef = db.collection("rooms").document(args.roomID)
        val channelDocref = docRef.collection("channel${args.currentID}")
            .document(args.currentID)
        registration.add(channelDocref.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val value = snapshot.toObject<PostData>()
                value?.number?.let {
                    opponentValue = it.toInt()
                    val index = bingoManager.getValues().indexOf(Node(it.toInt()))
                    updateBoard(bingoManager.getValues().get(index), NodeSelection.Opponent)
                    enable_disable(true)
                }
            }
        })

        registration.add(docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val roomDetials = snapshot.toObject<Room>()
                roomDetials?.let { it ->
                    if (it.roomStatus == RoomStatus.Finished || it.roomStatus == RoomStatus.QUIT) {

                        Toast.makeText(
                            requireContext(),
                            if (it.winBy == args.currentID) "You win the Game" else "You Lost the Game",
                            Toast.LENGTH_LONG
                        ).show()
                        it.winBy?.let { s ->
                            UpdateLeaderBoard(s)
                        }
                    }
                }

            }
        })
    }

    private fun UpdateLeaderBoard(userID: String) {
        val leaderdbRef = db.collection("leaderBoard").document(userID)
        leaderdbRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                it.result?.let {
                    if (it.exists())
                        leaderdbRef.update("roomIDs", FieldValue.arrayUnion(args.roomID))
                            .addOnCompleteListener {
                                showBackToHome()
                            }
                    else {
                        val docData = hashMapOf("roomIDs" to arrayListOf(args.roomID))
                        leaderdbRef.set(docData)
                            .addOnSuccessListener {
                                showBackToHome()
                            }
                    }
                }

            }
        }
    }

    private fun showBackToHome() {
        anim?.cancel()
        anim?.end()
        removeListener()
        findNavController().navigate(R.id.action_bingoBoardFragment_to_mainFragment)
    }

    private fun removeListener() {
        for (reg in registration)
            reg.remove()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}