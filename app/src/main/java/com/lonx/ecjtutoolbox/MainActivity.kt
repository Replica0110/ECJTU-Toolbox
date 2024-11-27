package com.lonx.ecjtutoolbox

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.drake.statusbar.immersive
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.databinding.ActivityMainBinding
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import com.lonx.ecjtutoolbox.utils.StuProfileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var stuId: String
    private lateinit var stuPassword: String
    private var isp = 1

    private lateinit var stuProfileInfo: StuProfileInfo

    private val jwxtApi: JWXTApi by inject()
    private val preferencesManager: PreferencesManager by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar, true)


        val navController = findNavController(R.id.nav)
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(binding.navView.menu, binding.drawer)
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.subtitle =
                (destination as FragmentNavigator.Destination).className.substringAfterLast('.')
        }

        binding.navView.setupWithNavController(navController)
        initLogin()
    }

    private fun initLogin(){
        stuId = preferencesManager.getString("student_id", "")
        stuPassword = preferencesManager.getString("student_pwd", "")
        isp = preferencesManager.getInt("isp", 1)
        if (stuId.isEmpty() || stuPassword.isEmpty()) {
            Toast.makeText(this, "请先设置学号和密码", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val loginResult = jwxtApi.login()
                withContext(Dispatchers.Main) {
                    binding.drawer.closeDrawers()
                    Toast.makeText(this@MainActivity, loginResult, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "未知错误：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    companion object {
        const val TAG = "MainActivity"
    }

}
