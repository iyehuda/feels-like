package com.iyehuda.feelslike.ui.newpost

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.iyehuda.feelslike.databinding.FragmentNewPostBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.utils.ImagePicker
import com.iyehuda.feelslike.ui.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewPostFragment : BaseFragment<FragmentNewPostBinding>() {

    private val viewModel: NewPostViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewPostBinding {
        return FragmentNewPostBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePicker = ImagePicker.create(this) { uri ->
            viewModel.setPostImage(uri)
        }

        viewModel.postImageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let { updateImageView(it) }
        }

        binding.imagePlaceholder.setOnClickListener {
            imagePicker.pickSingleImage()
        }

        binding.btnUploadPost.setOnClickListener {
            submitPost()
        }

        binding.btnCancel.setOnClickListener {
            goBack()
        }
    }

    private fun updateImageView(uri: Uri) {
        if (uri != Uri.EMPTY) {
            ImageUtil.loadImage(this, binding.imagePlaceholder, uri, true)
        }
    }

    private fun submitPost() {
        val postText = binding.etPostText.text.toString()

        binding.loadingProgressBar.visibility = View.VISIBLE

        viewModel.uploadPost(postText,
            onSuccess = {
                binding.loadingProgressBar.visibility = View.GONE
                goBack()
            },
            onError = { e ->
                binding.loadingProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}