package com.iyehuda.feelslike.ui.myprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

        myProfileViewModel.userDetails.observe(viewLifecycleOwner) {
            if (it == null) {
                findNavController().popBackStack()
            }
        }

        myProfileViewModel.text.observe(viewLifecycleOwner) {
            binding.textMyProfile.text = it
        }

        binding.logoutButton.setOnClickListener {
            myProfileViewModel.logout()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
