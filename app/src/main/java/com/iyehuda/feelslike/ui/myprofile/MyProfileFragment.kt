package com.iyehuda.feelslike.ui.myprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.databinding.FragmentMyProfileBinding
import com.iyehuda.feelslike.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : BaseFragment<FragmentMyProfileBinding>() {
    private val viewModel: MyProfileViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentMyProfileBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userDetails.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateUserView(it)
            }
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(R.id.action_edit_profile)
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_logout)
        }
    }

    private fun updateUserView(user: UserDetails) {
        Glide.with(this).load(user.photoUrl).circleCrop().into(binding.avatarImageView)
        binding.emailTextView.text = user.email
        binding.displayNameTextView.text = user.displayName
    }
}
