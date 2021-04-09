package com.manishk9.bingo.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.manishk9.bingo.R
import com.manishk9.bingo.ResultWrapper
import com.manishk9.bingo.Utils
import com.manishk9.bingo.auth.LoginViewModel
import com.manishk9.bingo.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val model: LoginViewModel by viewModels()

    private val callbackManager by lazy {
        CallbackManager.Factory.create()
    }

    private var progressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setPermissions("email", "public_profile")
        binding.loginButton.fragment = this

        binding.loginButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Toast.makeText(requireContext(), "facebook:onCancel", Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(requireContext(), "facebook:onError ${error}", Toast.LENGTH_LONG)
                    .show()

            }
        })

        binding.joinFac.setOnClickListener {
            model.response.postValue(ResultWrapper.Loading)
            binding.loginButton.performClick()
        }

        model.response.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ResultWrapper.Loading -> {
                    progressDialog ?: run {
                        progressDialog = Utils.progressDialog(requireContext())
                        progressDialog?.show()
                    }
                }
                is ResultWrapper.Error -> {
                    progressDialog?.dismiss()
                }
                is ResultWrapper.Success -> {
                    progressDialog?.dismiss()
                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                }
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        model.doLogin(token)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        progressDialog = null
    }
}