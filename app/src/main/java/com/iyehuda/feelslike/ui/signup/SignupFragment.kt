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
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.databinding.FragmentSignupBinding
import com.iyehuda.feelslike.ui.ViewModelFactory
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {
    private val viewModel: SignupViewModel by viewModels { ViewModelFactory() }
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
                binding.avatarImageButton.setImageURI(it)
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
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                param(FirebaseAnalytics.Param.ITEM_ID, "signup_button")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
                param(FirebaseAnalytics.Param.SCREEN_NAME, "signup")
            }
            performSignup()
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

    private fun performSignup() {
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

    private fun onSignupResult(signupResult: SignupResult) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, "email")
            param(FirebaseAnalytics.Param.SUCCESS, (signupResult.success != null).toString())
            param(FirebaseAnalytics.Param.SCREEN_NAME, "signup")

            signupResult.error?.let {
                param("error", getString(it))
            }
        }

        binding.loadingProgressBar.visibility = View.GONE
        signupResult.error?.let {
            onSignupFailed(it)
        }
        signupResult.success?.let {
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
