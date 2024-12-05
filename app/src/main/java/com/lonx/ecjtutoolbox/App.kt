package com.lonx.ecjtutoolbox

import android.app.Application
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor

import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.ui.account.AccountViewModel
import com.lonx.ecjtutoolbox.utils.MyOkHttpClient
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // Koin依赖注入配置
        val appModule = module {
            // 提供PreferencesManager
            single<PreferencesManager> { PreferencesManager.getInstance(androidContext()) }

            // 提供CookieJar（PersistentCookieJar实例）
            single<CookieJar> { PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(androidContext())) }

            // 提供HttpClient
            single<OkHttpClient> { MyOkHttpClient(get(), get()).createClient() }

            // 提供timeout值
            single<Long> { 30L }

            // 提供JWXTApi
            single<JWXTApi> {
                val preferencesManager: PreferencesManager = get()
                val stuId = preferencesManager.getString("student_id", "")
                val stuPassword = preferencesManager.getString("student_pwd", "")
                val cookieJar: CookieJar = get()
                val client: OkHttpClient = get()
                JWXTApi(stuId, stuPassword, cookieJar as PersistentCookieJar, client)
            }

            // 提供ViewModel
            viewModel { AccountViewModel(get()) }
        }

        // 启动Koin
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}
