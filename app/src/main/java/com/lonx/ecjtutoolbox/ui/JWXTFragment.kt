package com.lonx.ecjtutoolbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lonx.ecjtutoolbox.databinding.FragmentJwxtBinding
import com.lonx.ecjtutoolbox.adapters.ItemClickableAdapter
import com.lonx.ecjtutoolbox.viewmodels.JWXTViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
class JWXTFragment : Fragment() {

    private var _binding: FragmentJwxtBinding? = null
    private val jwxtViewModel: JWXTViewModel by viewModel()
    private lateinit var adapter: ItemClickableAdapter
    private val binding get() = _binding!!
    private fun setupRecyclerView() {
        adapter = ItemClickableAdapter(emptyList())
        binding.rvJwxt.adapter = adapter
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentJwxtBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = jwxtViewModel
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jwxtViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateData(items)
        }
        view.post {
            jwxtViewModel.loadItems()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}