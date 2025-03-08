package com.iyehuda.feelslike.ui.newpost

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.iyehuda.feelslike.databinding.FragmentNewPostBinding

class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NewPostViewModel

    // Register the image picker ActivityResultLauncher
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Update the image placeholder with the selected image
            binding.imagePlaceholder.setImageURI(it)
            // Pass the selected image URI to the ViewModel (for later upload, etc.)
            viewModel.setPostImage(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewPostViewModel::class.java)

        // Set a click listener on the image placeholder to launch the image picker
        binding.imagePlaceholder.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Existing logic for upload or cancel buttons…
        binding.btnUploadPost.setOnClickListener {
            val postText = binding.etPostText.text.toString()
            viewModel.uploadPost(postText)
        }

        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}