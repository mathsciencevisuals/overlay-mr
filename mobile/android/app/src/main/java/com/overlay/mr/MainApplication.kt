package com.overlay.mr

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.overlay.mr.bridge.MRPackage
import com.overlay.mr.frameprocessor.MRFrameProcessorPlugin

class MainApplication : Application(), ReactApplication {
    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages() = PackageList(this).packages.apply {
                add(MRPackage())
            }

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
            override fun getJSMainModuleName(): String = "index"
            override val isNewArchEnabled: Boolean = false
            override val isHermesEnabled: Boolean = true
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        MRFrameProcessorPlugin.register()
    }
}
