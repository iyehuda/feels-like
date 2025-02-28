package com.iyehuda.feelslike.ui.signup

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.explainableErrorOrNull
import com.iyehuda.feelslike.databinding.FragmentSignupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : Fragment() {
    private val viewModel: SignupViewModel by viewModels()
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePicker =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                viewModel.onAvatarSelected(uri)
                updateSignupData()
            }

        viewModel.selectedImageUri.observe(viewLifecycleOwner) {
            if (it != Uri.EMPTY) {
                Glide.with(this).load(it).circleCrop().into(binding.avatarImageButton)
            }
        }

        viewModel.signupFormState.observe(viewLifecycleOwner) {
            onSignupFormUpdated(it)
        }

        binding.avatarImageButton.setOnClickListener {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val afterTextChangedListener = { _: Editable? ->
            updateSignupData()
        }
        binding.displayNameEditText.doAfterTextChanged(afterTextChangedListener)
        binding.emailEditText.doAfterTextChanged(afterTextChangedListener)
        binding.passwordEditText.doAfterTextChanged(afterTextChangedListener)

        binding.signupButton.setOnClickListener {
            signup()
        }
    }

    private fun updateSignupData() {
        viewModel.signupDataChanged(
            binding.displayNameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.passwordEditText.text.toString(),
        )
    }

    private fun onSignupFormUpdated(signupFormState: SignupFormState) {
        binding.signupButton.isEnabled = signupFormState.isDataValid

        signupFormState.displayNameError?.let {
            binding.displayNameEditText.error = getString(it)
        }
        signupFormState.emailError?.let {
            binding.emailEditText.error = getString(it)
        }
        signupFormState.passwordError?.let {
            binding.passwordEditText.error = getString(it)
        }
    }

    private fun signup() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        viewModel.signup(
            binding.displayNameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.passwordEditText.text.toString(),
            viewModel.selectedImageUri.value!!,
        ) {
            viewLifecycleOwner.lifecycleScope.launch {
                onSignupResult(it)
            }
        }
    }

    private fun onSignupResult(signupResult: Result<UserDetails>) {
        binding.loadingProgressBar.visibility = View.GONE
        signupResult.explainableErrorOrNull()?.let {
            onSignupFailed(it)
        }
        signupResult.getOrNull()?.let {
            onSignupSucceeded(it)
        }
    }

    private fun onSignupSucceeded(model: UserDetails) {
        displayToast(getString(R.string.signup_greeting, model.displayName))
        findNavController().navigate(R.id.action_sign_up)
    }

    private fun onSignupFailed(@StringRes errorString: Int) = displayToast(getString(errorString))

    private fun displayToast(message: String) {
        context?.applicationContext?.let { appContext ->
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
