package com.iyehuda.feelslike.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.databinding.FragmentHomeBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        binding.fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_newPostFragment)
        }

        // Observe the posts LiveData.
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }
    }
}
