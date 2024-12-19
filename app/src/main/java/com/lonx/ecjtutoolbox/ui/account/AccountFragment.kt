package com.lonx.ecjtutoolbox.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.databinding.FragmentAccountBinding
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val ispOptions = arrayOf("中国电信", "中国移动", "中国联通")
    private var studId: String = ""
    private var stuPassword: String = ""
    private var isp: Int = 1
    private val jwxtApi: JWXTApi by inject()
    private val binding get() = _binding!!
    private val preferencesManager by lazy {
        PreferencesManager.getInstance(requireContext())
    }
    private val accountViewModel: AccountViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = accountViewModel
        // 加载用户数据
        accountViewModel.loadUserProfile()
        // 观察 LiveData 更新 UI
        accountViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.info = profile
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}