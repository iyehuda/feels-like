package com.iyehuda.feelslike.ui.newpost

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import com.iyehuda.feelslike.databinding.FragmentNewPostBinding
import com.iyehuda.feelslike.ui.base.BaseFragment

@AndroidEntryPoint
class NewPostFragment : BaseFragment<FragmentNewPostBinding>() {

    // Hilt view model injection
    private val viewModel: NewPostViewModel by viewModels()

    // Register the image picker ActivityResultLauncher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Update the image placeholder with the selected image
                binding.imagePlaceholder.setImageURI(it)
                // Pass the selected image URI to the ViewModel (for later upload, etc.)
                viewModel.setPostImage(it)
            }
        }

    // Inflate binding via the createBinding function from BaseFragment
    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewPostBinding {
        return FragmentNewPostBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener to launch the image picker
        binding.imagePlaceholder.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Upload post button: triggers the upload logic in the ViewModel.
        binding.btnUploadPost.setOnClickListener {
            val postText = binding.etPostText.text.toString()
            viewModel.uploadPost(postText,
                onSuccess = {
                    // Navigate back using BaseFragment's goBack method.
                    goBack()
                },
                onError = { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Cancel button: use BaseFragment's goBack method to navigate back.
        binding.btnCancel.setOnClickListener {
            goBack()
        }
    }
}