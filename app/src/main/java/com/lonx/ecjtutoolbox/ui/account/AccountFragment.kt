package com.lonx.ecjtutoolbox.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.databinding.FragmentAccountBinding
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        // 观察 LiveData 更新 UI
        accountViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.m = profile
        }

        // 加载用户数据
        accountViewModel.loadUserProfile()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_account_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add -> {
                addAccountDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun addAccountDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_account, null)

        val etStuId = view.findViewById<EditText>(R.id.account_stuid)
        val etStuPassword = view.findViewById<EditText>(R.id.account_passwrod)
        val spIsp = view.findViewById<Spinner>(R.id.account_isp)
        etStuId.setText(preferencesManager.getString("student_id", ""))
        etStuPassword.setText(preferencesManager.getString("student_pwd", ""))
        spIsp.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ispOptions).apply{
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spIsp.setSelection(preferencesManager.getInt("isp", 1) - 1)
        builder.setView(view)
            .setPositiveButton("保存并登录") { _, _ ->
                studId = etStuId.text.toString()
                stuPassword = etStuPassword.text.toString()
                isp = spIsp.selectedItemPosition + 1
                preferencesManager.putString("student_id", studId)
                preferencesManager.putString("student_pwd", stuPassword)
                preferencesManager.putInt("isp", isp)
                try {
                    lifecycleScope.launch(Dispatchers.IO) {
                        jwxtApi.updateInfo(studId,stuPassword) // 更新账号信息
                        val state = jwxtApi.login(true)
                        withContext(Dispatchers.Main) {Toast.makeText(requireContext(), state, Toast.LENGTH_LONG).show()}
                    }

                } catch (e: Exception){
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "出现错误：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}