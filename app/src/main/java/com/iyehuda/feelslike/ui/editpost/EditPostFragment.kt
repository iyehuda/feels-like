package com.iyehuda.feelslike.ui.editpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.iyehuda.feelslike.databinding.FragmentEditPostBinding
import com.iyehuda.feelslike.ui.base.BaseFragment

class EditPostFragment : BaseFragment<FragmentEditPostBinding>() {
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEditPostBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Remove cancel button functionality

        // Disable other buttons for now
        binding.btnUpdate.isEnabled = false
        binding.btnDelete.isEnabled = false
    }
} 