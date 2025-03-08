package com.iyehuda.feelslike.ui.newpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.iyehuda.feelslike.databinding.FragmentNewPostBinding

class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NewPostViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewPostViewModel::class.java)

        // Handle button clicks
        binding.btnUploadPost.setOnClickListener {
            val text = binding.etPostText.text.toString()
            viewModel.uploadPost(text)
            // Optionally navigate back or show a success message
        }

        binding.btnCancel.setOnClickListener {
            // Optionally navigate back or close the fragment
            requireActivity().onBackPressed()
        }

        // If you need to pick an image:
        // binding.imagePlaceholder.setOnClickListener {
        //     // Use your existing ImagePicker or any other approach
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}