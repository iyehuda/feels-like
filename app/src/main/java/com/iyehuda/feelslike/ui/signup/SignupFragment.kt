package com.iyehuda.feelslike.ui.signup

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.iyehuda.feelslike.ui.utils.ImagePicker
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

        val imagePicker = ImagePicker.create(this) { uri ->
            viewModel.updateAvatar(uri)
            updateFormData()
        }

        viewModel.selectedAvatar.observe(viewLifecycleOwner) {
            Glide.with(this).load(it).circleCrop().into(binding.chooseAvatarImageButton)
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

        // TODO: Use uiState
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
            onSubmitFailure(it)
        }

        signupResult.getOrNull()?.let {
            onSubmitSuccess(it)
        }
    }

    private fun onSubmitSuccess(model: UserDetails) {
        displayToast(getString(R.string.signup_greeting, model.displayName))
        findNavController().navigate(R.id.action_sign_up)
    }

    private fun onSubmitFailure(@StringRes errorString: Int) = displayToast(getString(errorString))

    // TODO: Move to FeelsLikeBaseFragment
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
