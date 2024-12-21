package com.lonx.ecjtutoolbox

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.drake.statusbar.immersive
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.databinding.ActivityMainBinding
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import slimber.log.e


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle // 声明 ActionBarDrawerToggle
    private val preferencesManager: PreferencesManager by lazy { PreferencesManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置 Toolbar
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar, true)

        // 初始化 ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawer,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        // 添加 DrawerLayout 的监听器
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()

        // 获取 NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // 绑定 NavController 到 Toolbar 和 NavigationView
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(binding.navView.menu, binding.drawer)
        )
        binding.navView.setupWithNavController(navController)

        // 初始化登录逻辑
        initLogin()
    }

    private fun initLogin() {
        val stuId = preferencesManager.getString("student_id", "")
        val stuPassword = preferencesManager.getString("student_pwd", "")
        val isp = preferencesManager.getInt("isp", 1)
        val refresh = preferencesManager.getBoolean("refresh_login", false)
        val isLogin = false

        if (isLogin && !refresh) {
            Toast.makeText(this, "已登录", Toast.LENGTH_SHORT).show()
            return
        }

        if (stuId.isEmpty() || stuPassword.isEmpty()) {
            Toast.makeText(this, "请先设置学号和密码", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val loginResult = "登录成功"
                withContext(Dispatchers.Main) {
                    binding.drawer.closeDrawers()
                    Toast.makeText(this@MainActivity, loginResult, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "未知错误：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 处理 ActionBarDrawerToggle 的按钮点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
