package com.example.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * خدمة أساسية لـ WebRTC لدعم البث الصوتي المباشر بين المستخدمين داخل الغرف (Wanas v3.1).
 *
 * تحافظ هذه الخدمة على نشاط جلسة WebRTC الصوتية واتصالات الـ PeerConnection
 * ونقل الصوت المباشر عبر الإنترنت داخل الغرف دون انقطاع، وتوفر نقطة وصول موحدة
 * لمحرك الصوت [RealVoiceRoomEngine].
 */
class WebRtcVoiceRoomService : Service() {

    inner class LocalVoiceBinder : Binder() {
        fun getService(): WebRtcVoiceRoomService = this@WebRtcVoiceRoomService
        fun getVoiceEngine(): RealVoiceRoomEngine = obtainEngine(applicationContext)
    }

    private val binder = LocalVoiceBinder()
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        obtainEngine(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_VOICE_STREAM
        when (action) {
            ACTION_STOP_VOICE_STREAM -> {
                releaseWakeLock()
                runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_VOICE_STREAM -> {
                val roomId = intent?.getStringExtra(EXTRA_ROOM_ID).orEmpty()
                val roomName = intent?.getStringExtra(EXTRA_ROOM_NAME).orEmpty().ifBlank { "غرفة وَنَس الصوتية" }
                acquirePartialWakeLock()
                startForegroundSafely(roomId, roomName)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    private fun startForegroundSafely(roomId: String, roomName: String) {
        try {
            val notification = buildVoiceStreamingNotification(roomId, roomName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Foreground notification start note: ${t.message}")
        }
    }

    private fun buildVoiceStreamingNotification(roomId: String, roomName: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROOM_ID, roomId)
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentIntent = PendingIntent.getActivity(this, 0, launchIntent, pendingFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🎙️ وَنَس v3.1 — بث صوتي مباشر (WebRTC)")
            .setContentText("متصل الآن في: $roomName")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "البث الصوتي المباشر للغرف (WebRTC v3.1)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "خدمة البث الصوتي المباشر عبر WebRTC داخل غرف تطبيق وَنَس v3.1"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun acquirePartialWakeLock() {
        try {
            if (wakeLock?.isHeld == true) return
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "WanasV31::WebRtcVoiceStreamWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(60 * 60 * 1000L) // Up to 60 minutes per room session
            }
        } catch (t: Throwable) {
            Log.w(TAG, "WakeLock note: ${t.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Throwable) {
        }
        wakeLock = null
    }

    companion object {
        private const val TAG = "WebRtcVoiceRoomService"
        private const val CHANNEL_ID = "wanas_webrtc_voice_channel_v31"
        private const val NOTIFICATION_ID = 3101

        const val ACTION_START_VOICE_STREAM = "com.example.voice.action.START_VOICE_STREAM"
        const val ACTION_STOP_VOICE_STREAM = "com.example.voice.action.STOP_VOICE_STREAM"
        const val EXTRA_ROOM_ID = "extra_room_id"
        const val EXTRA_ROOM_NAME = "extra_room_name"

        @Volatile
        private var sharedEngineInstance: RealVoiceRoomEngine? = null

        @Volatile
        var isVoiceRoomSessionActive: Boolean = false
            private set

        @Volatile
        var currentActiveRoomId: String = ""
            private set

        @Volatile
        var currentActiveRoomName: String = ""
            private set

        fun obtainEngine(context: Context): RealVoiceRoomEngine {
            return sharedEngineInstance ?: synchronized(this) {
                sharedEngineInstance ?: RealVoiceRoomEngine(context.applicationContext).also {
                    sharedEngineInstance = it
                }
            }
        }

        fun startVoiceStreamingService(
            context: Context,
            roomId: String,
            roomName: String = "غرفة وَنَس الصوتية"
        ) {
            try {
                currentActiveRoomId = roomId
                currentActiveRoomName = roomName.ifBlank { "غرفة وَنَس الصوتية" }
                isVoiceRoomSessionActive = true
                obtainEngine(context.applicationContext)
            } catch (t: Throwable) {
                Log.w(TAG, "startVoiceStreamingService note: ${t.message}")
            }
        }

        fun stopVoiceStreamingService(context: Context) {
            try {
                isVoiceRoomSessionActive = false
                currentActiveRoomId = ""
                currentActiveRoomName = ""
            } catch (_: Throwable) {
            }
        }
    }
}
