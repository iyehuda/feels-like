package com.iyehuda.feelslike.ui.signup

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.explainableErrorOrNull
import com.iyehuda.feelslike.databinding.FragmentSignupBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.utils.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : BaseFragment<FragmentSignupBinding>() {
    private val viewModel: SignupViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentSignupBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePicker = ImagePicker.create(this) { uri ->
            viewModel.updateAvatar(uri)
            updateFormData()
        }

        viewModel.selectedAvatar.observe(viewLifecycleOwner) {
            if (it != Uri.EMPTY) {
                Glide.with(this).load(it).circleCrop().into(binding.chooseAvatarImageButton)
            }
        }

        viewModel.formState.observe(viewLifecycleOwner) {
            onFormUpdated(it)
        }

        binding.chooseAvatarImageButton.setOnClickListener {
            imagePicker.pickSingleImage()
        }

        val afterTextChangedListener = { _: Editable? ->
            updateFormData()
        }
        binding.displayNameEditText.doAfterTextChanged(afterTextChangedListener)
        binding.emailEditText.doAfterTextChanged(afterTextChangedListener)
        binding.passwordEditText.doAfterTextChanged(afterTextChangedListener)

        binding.signupButton.setOnClickListener {
            submitForm()
        }
    }

    private fun updateFormData() {
        viewModel.updateFormData(
            binding.displayNameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.passwordEditText.text.toString(),
        )
    }

    private fun onFormUpdated(signupFormState: SignupFormState) {
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

    private fun submitForm() {
        binding.loadingProgressBar.visibility = View.VISIBLE

        viewModel.signup(
            binding.displayNameEditText.text.toString(),
            binding.emailEditText.text.toString(),
            binding.passwordEditText.text.toString(),
            viewModel.selectedAvatar.value!!,
        ) {
            viewLifecycleOwner.lifecycleScope.launch {
                onSubmitResult(it)
            }
        }
    }

    private fun onSubmitResult(signupResult: Result<UserDetails>) {
        binding.loadingProgressBar.visibility = View.GONE

        signupResult.explainableErrorOrNull()?.let {
            displayToast(it)
        }

        signupResult.getOrNull()?.let {
            displayToast(getString(R.string.signup_greeting, it.displayName))
            findNavController().navigate(R.id.action_sign_up)
        }
    }
}
