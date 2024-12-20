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
import org.koin.androidx.viewmodel.ext.android.viewModel

class WifiFragment : Fragment() {
    private lateinit var binding: FragmentWifiBinding
    private val wifiViewModel: WifiViewModel by viewModel()
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化 DataBinding
        binding = FragmentWifiBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = wifiViewModel
        return binding.root
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
        view.post {
            // 订阅状态
            wifiViewModel.observeStatuses(requireContext())
        }
    }
}
