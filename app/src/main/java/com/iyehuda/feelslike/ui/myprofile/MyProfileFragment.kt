package com.iyehuda.feelslike.ui.myprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.data.model.UserDetails
import com.iyehuda.feelslike.databinding.FragmentMyProfileBinding
import com.iyehuda.feelslike.ui.ViewModelFactory

class MyProfileFragment : Fragment() {
    private val myProfileViewModel: MyProfileViewModel by viewModels { ViewModelFactory() }
    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        myProfileViewModel.userDetails.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateUserView(it)
            }
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        return root
    }

    private fun updateUserView(user: UserDetails) {
        binding.emailTextView.text = getString(R.string.profile_email_text, user.email)
        binding.displayNameTextView.text =
            getString(R.string.profile_display_name_text, user.displayName)
        binding.photoUrlTextView.text = getString(R.string.profile_photo_url_text, user.photoUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        myProfileViewModel.logout()
        findNavController().navigate(R.id.action_logout)
    }
}
