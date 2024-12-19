package com.lonx.ecjtutoolbox.ui.wifi

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
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
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        return locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ?: false ||
                locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) ?: false
    }
    private fun checkAndRequestPermissions() {
        val context = requireContext() // 检查是否已开启位置信息
        if (!isLocationEnabled(context)) {
            AlertDialog.Builder(context).apply {
                setTitle("需要开启位置信息")
                setMessage("应用需要您开启位置信息服务以获取WiFi信息，是否前往设置开启？")
                setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }
                setNegativeButton("取消", null)
                show()
            }
            return
        }
        // 检查是否已经授予位置权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 如果用户之前拒绝了权限请求，显示说明对话框
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as android.app.Activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(context).apply {
                    setTitle("需要位置权限")
                    setMessage("应用需要位置权限以获取WiFi信息，请在权限设置中启用此权限。")
                    setPositiveButton("去设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                    setNegativeButton("取消", null)
                    show()
                }
            } else {
                // 未请求过权限，或用户选择了"不再提示"，直接请求权限
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            // 检查并请求权限
            checkAndRequestPermissions()
            wifiViewModel.observeWifiStatus(requireContext())
        }
    }
}
