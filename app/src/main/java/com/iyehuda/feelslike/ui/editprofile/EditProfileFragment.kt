package com.iyehuda.feelslike.ui.editprofile

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
import com.iyehuda.feelslike.databinding.FragmentEditProfileBinding
import com.iyehuda.feelslike.ui.utils.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private val viewModel: EditProfileViewModel by viewModels()
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePicker = ImagePicker.create(this) { uri ->
            viewModel.updateAvatar(uri)
            updateFormData()
        }

        viewModel.userDetails.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateUserView(it)
            }
        }

        viewModel.selectedAvatar.observe(viewLifecycleOwner) {
            Glide.with(this).load(it).circleCrop().into(binding.editAvatarImageButton)
        }

        viewModel.formState.observe(viewLifecycleOwner) {
            onFormUpdated(it)
        }

        binding.editAvatarImageButton.setOnClickListener {
            imagePicker.pickSingleImage()
        }

        val afterTextChangedListener = { _: Editable? ->
            updateFormData()
        }
        binding.displayNameEditText.doAfterTextChanged(afterTextChangedListener)

        binding.saveButton.setOnClickListener {
            submitForm()
        }

        binding.cancelButton.setOnClickListener {
            goBack()
        }
    }

    private fun updateUserView(user: UserDetails) {
        viewModel.updateAvatar(user.photoUrl)
        binding.emailTextView.text = user.email
        binding.displayNameTextView.text = user.displayName
        binding.displayNameEditText.setText(user.displayName)
    }

    private fun updateFormData() {
        viewModel.updateFormData(binding.displayNameEditText.text.toString())
    }

    private fun onFormUpdated(signupFormState: EditProfileFormState) {
        binding.saveButton.isEnabled = signupFormState.isDataValid

        signupFormState.displayNameError?.let {
            binding.displayNameEditText.error = getString(it)
        }
    }

    private fun submitForm() {
        binding.loadingProgressBar.visibility = View.VISIBLE

        // TODO: Use uiState
        viewModel.updateProfile(
            binding.displayNameEditText.text.toString(),
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
            onSubmitSuccess()
        }
    }

    private fun onSubmitSuccess() {
        displayToast(getString(R.string.profile_edit_succeeded))
        goBack()
    }

    private fun onSubmitFailure(@StringRes errorString: Int) = displayToast(getString(errorString))

    // TODO: Move to FeelsLikeBaseFragment
    private fun goBack() {
        findNavController().popBackStack()
    }

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
