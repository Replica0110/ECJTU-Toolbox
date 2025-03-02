package com.lonx.ecjtutoolbox.ui.wifi

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.lonx.ecjtutoolbox.databinding.FragmentWifiBinding
import com.lonx.ecjtutoolbox.utils.ItemClickableAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class WifiFragment : Fragment() {
    private var _binding: FragmentWifiBinding? = null
    private val binding get() = _binding!!
    private val wifiViewModel: WifiViewModel by viewModel()
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var adapter: ItemClickableAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化 DataBinding
        _binding = FragmentWifiBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = wifiViewModel
        setupRecyclerView()
        return binding.root
    }
    private fun setupRecyclerView() {
        adapter = ItemClickableAdapter(emptyList())
        binding.rvWifi.adapter = adapter
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 权限已授予，更新 SSID
                wifiViewModel.updateSSID(requireContext())
            } else {
                // 权限被拒绝
                wifiViewModel.ssid1.set("未授予位置权限")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiViewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.updateData(items)
        }
        view.post {
            wifiViewModel.loadItems()
            wifiViewModel.observeStatuses(requireContext())
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
