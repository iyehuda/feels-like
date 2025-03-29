package com.iyehuda.feelslike.ui.myprofile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.databinding.FragmentMyProfileBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import com.iyehuda.feelslike.ui.home.PostAdapter
import com.iyehuda.feelslike.ui.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : BaseFragment<FragmentMyProfileBinding>() {
    private val TAG = "MyProfileFragment"
    private val viewModel: MyProfileViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentMyProfileBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.userDetails.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateUserView(it)
            }
        }

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
            Log.d(TAG, "Received ${posts.size} posts")

            // Show/hide no posts message based on list size
            binding.noPostsTextView.isVisible = posts.isEmpty()
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_edit_profile)
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_logout)
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.userPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun updateUserView(user: UserDetails) {
        ImageUtil.loadImage(this, binding.avatarImageView, user.photoUrl, true)
        binding.emailTextView.text = user.email
        binding.displayNameTextView.text = user.displayName
    }
}
