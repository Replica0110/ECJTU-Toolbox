package com.lonx.ecjtutoolbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lonx.ecjtutoolbox.databinding.FragmentJwxtBinding
import com.lonx.ecjtutoolbox.viewmodels.JWXTViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
class JWXTFragment : Fragment() {

    private var _binding: FragmentJwxtBinding? = null
    private val jwxtViewModel: JWXTViewModel by viewModel()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentJwxtBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = jwxtViewModel
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}