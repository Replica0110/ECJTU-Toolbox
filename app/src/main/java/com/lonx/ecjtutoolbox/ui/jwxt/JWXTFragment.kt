package com.lonx.ecjtutoolbox.ui.jwxt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lonx.ecjtutoolbox.databinding.FragmentJwxtBinding

class JWXTFragment : Fragment() {

    private var _binding: FragmentJwxtBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[JWXTViewModel::class.java]

        _binding = FragmentJwxtBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textJwxt
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}