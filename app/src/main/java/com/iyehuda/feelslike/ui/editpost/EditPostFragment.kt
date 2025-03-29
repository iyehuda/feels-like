package com.iyehuda.feelslike.ui.editpost

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.iyehuda.feelslike.databinding.FragmentEditPostBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.utils.ImageUtil
import com.iyehuda.feelslike.ui.utils.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPostFragment : BaseFragment<FragmentEditPostBinding>() {
    
    private val viewModel: EditPostViewModel by viewModels()
    private val args: EditPostFragmentArgs by navArgs()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEditPostBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePicker = ImagePicker.create(this) { uri ->
            viewModel.setNewImage(uri)
            updateImageView(uri)
        }

        // Load post data
        viewModel.loadPost(args.postId)

        // Observe post data
        viewModel.post.observe(viewLifecycleOwner) { post ->
            post?.let {
                binding.apply {
                    etPostText.setText(it.description)
                    // Load image if exists, converting the URL string to Uri
                    it.imageUrl?.let { url ->
                        ImageUtil.loadImage(this@EditPostFragment, imagePlaceholder, url.toUri())
                    }
                    // Enable buttons since we have content
                    btnUpdate.isEnabled = true
                    btnDelete.isEnabled = true
                }
            }
        }

        // Add image click listener
        binding.imagePlaceholder.setOnClickListener {
            imagePicker.pickSingleImage()
        }

        // Add update button click listener
        binding.btnUpdate.setOnClickListener {
            updatePost()
        }

        // Add delete button click listener
        binding.btnDelete.setOnClickListener {
            deletePost()
        }
    }

    private fun updateImageView(uri: Uri) {
        if (uri != Uri.EMPTY) {
            ImageUtil.loadImage(this, binding.imagePlaceholder, uri)
        }
    }

    private fun updatePost() {
        val newDescription = binding.etPostText.text.toString().trim()
        if (newDescription.isEmpty()) {
            displayToast("Post description cannot be empty")
            return
        }

        lifecycleScope.launch {
            try {
                viewModel.updatePost(args.postId, newDescription).onSuccess {
                    displayToast("Post updated successfully")
                    findNavController().navigateUp()
                }.onFailure { e ->
                    displayToast("Failed to update post: ${e.message}")
                }
            } catch (e: Exception) {
                displayToast("An error occurred while updating the post")
            }
        }
    }

    private fun deletePost() {
        lifecycleScope.launch {
            try {
                viewModel.deletePost(args.postId).onSuccess {
                    // Show success message
                    displayToast("Post deleted successfully")
                    // Navigate back
                    findNavController().navigateUp()
                }.onFailure { e ->
                    // Show error message
                    displayToast("Failed to delete post: ${e.message}")
                }
            } catch (e: Exception) {
                displayToast("An error occurred while deleting the post")
            }
        }
    }

} 