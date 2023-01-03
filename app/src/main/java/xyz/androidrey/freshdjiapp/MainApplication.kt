package xyz.androidrey.freshdjiapp

import android.app.Application
import android.content.Context
import com.secneo.sdk.Helper

class MainApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Helper.install(this)
    }
}