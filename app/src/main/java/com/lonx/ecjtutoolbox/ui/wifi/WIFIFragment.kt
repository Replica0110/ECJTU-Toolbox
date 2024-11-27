package com.lonx.ecjtutoolbox.ui.wifi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lonx.ecjtutoolbox.databinding.FragmentWifiBinding

class WIFIFragment : Fragment() {
    private var _binding: FragmentWifiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel =
            ViewModelProvider(this)[WIFIViewModel::class.java]

        _binding = FragmentWifiBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textWifi
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}