package xyz.androidrey.freshdjiapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dji.v5.ux.core.panel.topbar.TopBarPanelWidget

class WidgetTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget)
        var topBarWidget = findViewById<TopBarPanelWidget>(R.id.tPanel)
        var systemStatusWidget = topBarWidget.systemStatusWidget
        systemStatusWidget?.let {

        }
    }
}