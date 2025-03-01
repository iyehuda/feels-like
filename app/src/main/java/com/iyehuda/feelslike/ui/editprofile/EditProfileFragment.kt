package com.iyehuda.feelslike.ui.editprofile

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.data.utils.explainableErrorOrNull
import com.iyehuda.feelslike.databinding.FragmentEditProfileBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.utils.ImagePicker
import com.iyehuda.feelslike.ui.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>() {
    private val viewModel: EditProfileViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentEditProfileBinding.inflate(inflater, container, false)

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
            if (it != Uri.EMPTY) {
                ImageUtil.loadImage(this, binding.editAvatarImageButton, it, true)
            }
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
            displayToast(it)
        }

        signupResult.getOrNull()?.let {
            displayToast(R.string.profile_edit_succeeded)
            goBack()
        }
    }
}
