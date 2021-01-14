package com.justai.aimybox.assistant

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.justai.aimybox.api.aimybox.AimyboxDialogApi
import com.justai.aimybox.assistant.skills.AlarmSkill
import com.justai.aimybox.assistant.skills.ReminderSkill
import com.justai.aimybox.assistant.skills.SettingsSkill
import com.justai.aimybox.assistant.skills.TimerSkill
import com.justai.aimybox.components.AimyboxAssistantFragment
import com.justai.aimybox.components.extensions.isPermissionGranted
import com.justai.aimybox.core.Config
import kotlinx.android.synthetic.main.layout_activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private var lastCheckedBotId = R.id.first_bot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val assistantFragment = AimyboxAssistantFragment()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.assistant_container, assistantFragment)
            commit()
        }

        /*bot_group.check(R.id.first_bot)
        bot_group.setOnCheckedChangeListener { _, checkedId ->
            if (lastCheckedBotId == checkedId) return@setOnCheckedChangeListener
            lastCheckedBotId = checkedId
            val app = application as? AimyboxApplication ?: return@setOnCheckedChangeListener
            when(checkedId) {
                R.id.first_bot -> {
                    launch {
                        app.aimybox.updateConfiguration(app.firstAimyboxConfig)
                    }
                }
                else -> {
                    launch {
                        app.aimybox.updateConfiguration(app.secondAimyboxConfig)
                    }
                }
            }
            Toast.makeText(this, "Подождите, зовём другого бота", Toast.LENGTH_SHORT).show()
        }*/
        withAudioPermission {
            (application as? AimyboxApplication)?.listenTriggers()
        }
    }

    override fun onBackPressed() {
        val assistantFragment = (supportFragmentManager.findFragmentById(R.id.assistant_container)
                as? AimyboxAssistantFragment)
        if (assistantFragment?.onBackPressed() != true) super.onBackPressed()
    }

    private fun withAudioPermission(action: () -> Unit) {
        if (this.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
            action()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 100)
            }
        }
    }

}