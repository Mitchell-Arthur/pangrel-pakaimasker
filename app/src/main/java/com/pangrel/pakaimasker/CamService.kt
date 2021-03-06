package com.pangrel.pakaimasker

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CamService() : Service() {
    private lateinit var database: DatabaseReference
    private var classificationModule : PyObject? = null
    private var captureHandler : CaptureHandler? = null
    private var locationTracker : LocationTracker? = null
    private var receiver: BroadcastReceiver? = null
    private var monitorTask: Runnable? = null
    private var mainHandler = Handler(Looper.getMainLooper())

    private var SCHEDULED_TIME_BEGIN : LocalTime? = null
    private var SCHEDULED_TIME_END : LocalTime? = null
    private var TOTAL_CAPTURE = 7                           // 7 images to capture
    private var TOTAL_CAPUTRE_PROCESSED = 2                 // 2 images sample to processed
    private var CAPUTRE_INTERVAL = 15 * 1000L               // 15 seconds capture interval
    private var LOCATION_INTERVAL = 5 * 1000L               // 7.5 seconds location updates
    private val SMALL_DISPLACEMENT_DISTANCE: Float = 20f    // 20 meters minimum distance to update
    private val SAFEZONE_RADIUS = 100                       // 50 meters radius of safezone
    private val CONFIDENCE_THRESHOLD = 0.8                  // 80 percent minimum confidence
    private var IS_ALERT_ENABLED = true                     // notification is shown

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        classificationModule = Python.getInstance().getModule("classification")

        sendBroadcast(Intent(ACTION_PREPARED))
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onCreate() {
        super.onCreate()

        init()

        val filter = IntentFilter()
        filter.addAction(ACTION_MONITOR)
        filter.addAction(ACTION_UPDATE_SAFEZONE)
        filter.addAction(ACTION_UPDATE_CONFIG)
        registerReceiver(receiver, filter)

        startForeground()
    }
    override fun onDestroy() {
        super.onDestroy()

        mainHandler.removeCallbacks(monitorTask)
        locationTracker?.stopMonitoring()
        captureHandler?.destroy()
        unregisterReceiver(receiver)
        sendBroadcast(Intent(ACTION_STOPPED))
    }


    private fun init() {
        val instance = FirebaseDatabase.getInstance()
        database = instance.getReference()
        locationTracker = LocationTracker(applicationContext, LOCATION_INTERVAL, SAFEZONE_RADIUS, SMALL_DISPLACEMENT_DISTANCE)
        captureHandler = CaptureHandler(applicationContext, TOTAL_CAPTURE, TOTAL_CAPUTRE_PROCESSED)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                Log.d(TAG, "Receive: " + p1?.action)
                when (p1?.action) {
                    ACTION_MONITOR -> startMonitoring()
                    ACTION_UPDATE_SAFEZONE -> {
                        locationTracker?.updateSafeZones(p1.getParcelableArrayListExtra<Zone>("zones"))
                    }
                    ACTION_UPDATE_CONFIG -> {
                        CAPUTRE_INTERVAL = p1.getLongExtra("interval", CAPUTRE_INTERVAL)
                        LOCATION_INTERVAL = (p1.getLongExtra("interval", (LOCATION_INTERVAL * 3)) / 3)
                        IS_ALERT_ENABLED = p1.getBooleanExtra("alert", IS_ALERT_ENABLED)

                        val scheduledTimeBegin = p1.getStringArrayExtra("time").first()
                        val scheduledTimeEnd = p1.getStringArrayExtra("time").last()

                        SCHEDULED_TIME_BEGIN = if (scheduledTimeBegin != "") LocalTime.parse(scheduledTimeBegin) else null
                        SCHEDULED_TIME_END = if (scheduledTimeEnd != "") LocalTime.parse(scheduledTimeEnd) else null

                        Log.d(TAG, "CAPUTRE_INTERVAL = $CAPUTRE_INTERVAL")
                        Log.d(TAG, "LOCATION_INTERVAL = $LOCATION_INTERVAL")
                        Log.d(TAG, "IS_ALERT_ENABLED = $IS_ALERT_ENABLED")
                        Log.d(TAG, "SCHEDULED_TIME_BEGIN = $scheduledTimeBegin")
                        Log.d(TAG, "SCHEDULED_TIME_END = $scheduledTimeEnd")
                    }
                }
            }
        }
        monitorTask = Runnable {
            mainHandler.postDelayed(monitorTask, CAPUTRE_INTERVAL)

            if (isReady()) {
                captureHandler?.captureRequest()
            }
        }

        locationTracker?.setListener(object: OnLocationListener {
            override fun onUpdated(safeZones: ArrayList<Zone>, isSafe: Boolean) {
                updateLocations(safeZones, isSafe)
            }
        })
        captureHandler?.setListener(object: OnCapturedListener {
            override fun onCapturedDone(bitmapList: ArrayList<Bitmap>) {
                executeClassification(bitmapList)
            }
        })
    }
    private fun isReady(): Boolean {
        if (SCHEDULED_TIME_BEGIN !== null && SCHEDULED_TIME_END != null) {
            val now = LocalTime.now()
            if (!(SCHEDULED_TIME_BEGIN!!.isBefore(now) && SCHEDULED_TIME_END!!.isAfter(now))) {
                Log.d(TAG, "Out of scheduled time-range")
                sendBroadcast(Intent(ACTION_SKIP_SCHEDULE))
                return false
            }
        }

        if (locationTracker?.isSafe() === true) {
            Log.d(TAG, "User is in safe-zone")
            val intent = Intent(ACTION_SKIP_SAFEZONE)
            intent.putExtra("safePlace", locationTracker?.getSafePlace())
            sendBroadcast(intent)
            return false
        }

        if (!(getSystemService(POWER_SERVICE) as PowerManager).isInteractive) {
            Log.d(TAG, "Screen device is off")
            return false
        }

        return true
    }
    private fun startMonitoring() {
        mainHandler.post(monitorTask)
    }
    private fun startForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mask Monitoring")
            .setContentText("Service is running in background...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.app_name))
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }
    private fun updateLocations(safeZones: ArrayList<Zone>, isSafe: Boolean) {
        val intent = Intent(ACTION_LOCATION)
        intent.putExtra("zones", safeZones)
        intent.putExtra("safe", isSafe)
        sendBroadcast(intent)
    }
    private fun executeClassification(images: MutableList<Bitmap>) {
        val classification = ImageClassification()
        classification.setModule(classificationModule!!)
        classification.setCallback(object : OnEventListener<ClassificationResult> {
            override fun onFailure(e: java.lang.Exception?) {
                Toast.makeText(
                    applicationContext,
                    "ERROR CLASSIFICATION: " + e!!.message,
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            override fun onSuccess(result: ClassificationResult) {
                val isPassed = result.accuracy >= CONFIDENCE_THRESHOLD

                val uid = FirebaseAuth.getInstance().uid
                if (uid != null) {
                    val date = LocalDate.now().toString()
                    val time = LocalTime.now().toString()

                    if (result.classification == ImageClassification.WITHOUT_MASK || result.classification == ImageClassification.WITH_MASK) {
                        val updates: MutableMap<String, Any> = HashMap()
                        updates["summaries/$uid/$date/totalScanned"] = ServerValue.increment(1)
                        if (result.classification == ImageClassification.WITHOUT_MASK)
                            updates["summaries/$uid/$date/totalUnmasked"] = ServerValue.increment(1)
                        else
                            updates["summaries/$uid/$date/totalMasked"] = ServerValue.increment(1)
                        updates["summaries/$uid/$date/lastUpdate"] = ServerValue.TIMESTAMP
                        database.updateChildren(updates)
                    }

                    database.child("scans").child(uid).child(date).push().setValue(Scanning(time, result, isPassed))
                    if (isPassed && (result.classification == ImageClassification.WITH_MASK || result.classification == ImageClassification.WITHOUT_MASK))
                        database.child("users").child(uid).child("last").setValue(LastScan(LocalDateTime.now().toString(), result.classification == ImageClassification.WITH_MASK))
                }

                if (!isReady()) {
                    return
                }

                val intent = Intent(ACTION_RESULT)
//                intent.putExtra("image", result.image)
                intent.putExtra("class", result.classification)
                intent.putExtra("accuracy", result.accuracy)
                intent.putExtra("passed", isPassed)
                sendBroadcast(intent)

                val notificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val cls = intent.getIntExtra("class", -1)
                if (cls === 1 && IS_ALERT_ENABLED) {
                    val notificationIntent = Intent(applicationContext, HomeActivity::class.java)
                    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                    notificationIntent.action = Intent.ACTION_MAIN
                    notificationIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    val resultIntent =
                        PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)

                    val notificationBuilder =
                        NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentIntent(resultIntent)
                            .setContentTitle("MASK REMINDER !!!")
                            .setContentText("Please Wear a Mask to Help Protect You Againts Coronavirus")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_REMINDER)

                    notificationManager.notify(
                        REMINDER_NOTIFICATION_ID,
                        notificationBuilder.build()
                    )
                } else {
                    notificationManager.cancel(REMINDER_NOTIFICATION_ID)
                }
            }
        })
        classification.execute(
            images.subList(
                Math.max(images.size - TOTAL_CAPUTRE_PROCESSED, 0),
                images.size
            )
        )
    }

    companion object {
        const val TAG = "CamService"

        const val ACTION_PREPARED = "pakaimasker.action.PREPARED"
        const val ACTION_STOPPED = "pakaimasker.action.STOPPED"
        const val ACTION_RESULT = "pakaimasker.action.RESULT"
        const val ACTION_LOCATION = "pakaimasker.action.LOCATION"
        const val ACTION_MONITOR = "pakaimasker.action.MONITOR"
        const val ACTION_UPDATE_SAFEZONE = "pakaimasker.action.UPDATE_SAFEZONE"
        const val ACTION_UPDATE_CONFIG = "pakaimasker.action.UPDATE_CONFIG"
        const val ACTION_SKIP_SAFEZONE = "pakaimasker.ACTION_SKIP_SAFEZONE"
        const val ACTION_SKIP_SCHEDULE = "pakaimasker.ACTION_SKIP_SCHEDULE"

        const val CHANNEL_ID = "cam_service_channel_id"
        const val CHANNEL_NAME = "cam_service_channel_name"
        const val ONGOING_NOTIFICATION_ID = 6660

        const val REMINDER_CHANNEL_ID = "cam_service_reminder_channel_id"
        const val REMINDER_CHANNEL_NAME = "cam_service_reminder_channel_name"
        const val REMINDER_NOTIFICATION_ID = 6665
    }
}