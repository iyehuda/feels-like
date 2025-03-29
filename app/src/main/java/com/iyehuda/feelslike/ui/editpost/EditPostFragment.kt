package com.iyehuda.feelslike.ui.editpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.iyehuda.feelslike.databinding.FragmentEditPostBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
    }
} 