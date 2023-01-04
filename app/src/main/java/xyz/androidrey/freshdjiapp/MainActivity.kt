package xyz.androidrey.freshdjiapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import dji.v5.ux.sample.showcase.defaultlayout.DefaultLayoutActivity

private const val TAG = "myApp"

class MainActivity : AppCompatActivity() {

    lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerApp()
        button = findViewById(R.id.bNext)
        button.setOnClickListener {
            startActivity(Intent(this, WidgetTestActivity::class.java))
        }

    }

    private fun registerApp() {
        SDKManager.getInstance().init(this, object : SDKManagerCallback {
            override fun onRegisterSuccess() {
                Log.e(TAG, "myApp onRegisterSuccess")
                runOnUiThread {
                    button.isEnabled = true
                }

            }

            override fun onRegisterFailure(error: IDJIError?) {
                Log.e(TAG, "myApp onRegisterFailure")
            }

            override fun onProductDisconnect(productId: Int) {
                Log.e(TAG, "myApp onProductDisconnect")
            }

            override fun onProductConnect(productId: Int) {
                Log.e(TAG, "myApp onProductConnect")
            }

            override fun onProductChanged(productId: Int) {
                Log.e(TAG, "myApp onProductChanged")
            }

            override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {
                Log.e(TAG, "myApp on InitProcess")
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    Log.e(TAG, "myapp start registerapp")
                    SDKManager.getInstance().registerApp()
                }
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                Log.e(TAG, "myApp onDatabaseDownloadProgress ")
            }

        })

    }
}