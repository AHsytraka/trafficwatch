package com.example.trafficwatch

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

/*
  DOCS :
    https://developer.android.com/reference/kotlin/android/content/Context
    https://developer.android.com/reference/kotlin/android/app/usage/NetworkStatsManager.html
    https://developer.android.com/reference/kotlin/android/app/usage/NetworkStats.Bucket
*/


class MainActivity: FlutterActivity() {
  private val CHANNEL = "samples.flutter.dev/battery"
  private val NETSTAT_CHANNEL = "samples.flutter.dev/networkStats"

  private fun getBatteryLevel(): Int {
    val batteryLevel: Int
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
      batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    } else {
      val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
      batteryLevel = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    }

    return batteryLevel
  }
  
    private fun getNetworkStats(): Long {
        val networkStatsManager = getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val bucket: NetworkStats.Bucket = networkStatsManager.querySummaryForDevice(
          NetworkStats.Bucket.DEFAULT_NETWORK_ALL, //networkType
          "", //subscriberId
          0, //startTime
          0 //endTime
          )
        return bucket.rxBytes + bucket.txBytes
    }
  
  
  override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {

    super.configureFlutterEngine(flutterEngine)

    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
      call, result ->      
      if (call.method == "getBatteryLevel") {
        val batteryLevel = getBatteryLevel()

        if (batteryLevel != -1) {
          result.success(batteryLevel)
        } else {
          result.error("UNAVAILABLE", "Battery level not available.", null)
        }
      } else {
        result.notImplemented()
      }
    }
    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, NETSTAT_CHANNEL).setMethodCallHandler { 
      call, result ->
        if (call.method == "getNetworkStats") {
          val networkStats = getNetworkStats()
          result.success(networkStats)
          } else {
            result.notImplemented()
          }
        }
  }
}
