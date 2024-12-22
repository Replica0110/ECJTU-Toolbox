package com.lonx.ecjtutoolbox

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.ui.account.AccountViewModel
import com.lonx.ecjtutoolbox.ui.wifi.WifiFragment
import com.lonx.ecjtutoolbox.ui.wifi.WifiViewModel
import com.lonx.ecjtutoolbox.utils.LocationStatusMonitor
import com.lonx.ecjtutoolbox.utils.MyOkHttpClient
import com.lonx.ecjtutoolbox.utils.PersistentCookieJar
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import com.lonx.ecjtutoolbox.utils.SharedPrefsCookiePersistor
import com.lonx.ecjtutoolbox.utils.WifiStatusMonitor
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import timber.log.Timber

class App: Application() {
    override fun onCreate() {
        super.onCreate()
            Timber.plant(Timber.DebugTree())

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

            // 提供 WifiManager 和 ConnectivityManager
            single { androidContext().getSystemService(Context.WIFI_SERVICE) as WifiManager }
            single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

            // 提供 WifiStatusMonitor
            single { WifiStatusMonitor(get(), get(),applicationContext) }
            // 提供 LocationStatusMonitor
            single { LocationStatusMonitor(applicationContext) }
            factory { WifiFragment() }
            // 提供 ViewModel
            viewModel { WifiViewModel(jwxtApi = get(), wifiStatusMonitor = get(), locationStatusMonitor = get(), preferencesManager = get()) }
            viewModel { AccountViewModel(jwxtApi = get(), preferencesManager = get()) }
        }

        // 启动Koin
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}
