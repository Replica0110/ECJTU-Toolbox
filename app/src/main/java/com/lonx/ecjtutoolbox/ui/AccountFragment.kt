package com.lonx.ecjtutoolbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lonx.ecjtutoolbox.databinding.FragmentAccountBinding
import com.lonx.ecjtutoolbox.viewmodels.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = accountViewModel
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 加载用户数据
        accountViewModel.loadUserProfile()
        // 观察 LiveData 更新 UI
        accountViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.info = profile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}