package com.iyehuda.feelslike.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iyehuda.feelslike.R
import com.iyehuda.feelslike.databinding.FragmentSplashBinding
import com.iyehuda.feelslike.ui.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    private val viewModel: SplashViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentSplashBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(1000)

            viewModel.userDetails.observe(viewLifecycleOwner) { user ->
                if (user == null) {
                    findNavController().navigate(R.id.action_enter_unauthenticated)
                } else {
                    findNavController().navigate(R.id.action_enter_authenticated)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("loaded", true)
    }
}
