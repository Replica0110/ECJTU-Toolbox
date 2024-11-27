package com.lonx.ecjtutoolbox

import android.app.Application

import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.ui.account.AccountViewModel
import com.lonx.ecjtutoolbox.utils.PreferencesManager
//import com.scwang.smart.refresh.footer.ClassicsFooter
//import com.scwang.smart.refresh.header.MaterialHeader
//import com.scwang.smart.refresh.layout.SmartRefreshLayout
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class App:Application() {
    override fun onCreate() {
        super.onCreate()

        // Koin依赖注入配置
        val appModule = module {
            single { PreferencesManager.getInstance(androidContext()) }
            single {
                val preferencesManager: PreferencesManager = get()
                val stuId = preferencesManager.getString("student_id", "")
                val stuPassword = preferencesManager.getString("student_pwd", "")
                JWXTApi(stuId, stuPassword)
            }
            viewModel { AccountViewModel(get()) }
        }
        startKoin{
            androidContext(this@App)
            modules(appModule)
        }

    }
}
