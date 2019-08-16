package com.retrox.aodmod.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.media.AudioAttributes
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.TextView
import com.retrox.aodmod.MainHook
import com.retrox.aodmod.pref.XPref
import com.retrox.aodmod.proxy.view.theme.ThemeClockPack
import com.retrox.aodmod.proxy.view.theme.ThemeManager
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.InvocationTargetException
import java.net.NetworkInterface
import java.net.NetworkInterface.getNetworkInterfaces
import java.security.Security
import java.util.*


fun TextView.setGoogleSans(style: String = "Regular"): Boolean {
    if (XPref.getFontWithSystem()) return false // 跟随系统字体
    typeface = Typeface.createFromAsset(ResourceUtils.getInstance(context).assets, "fonts/GoogleSans-$style.ttf")
    return true
}

fun Context.checkPermission(callback: () -> Unit): Boolean {
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        callback()
        return false
    } else return true
}

/**
 * 使用WakeLock来保证一些操作的正确执行
 */
fun Context.wakeLockWrap(tag: String, block: () -> Unit) {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
    wakeLock.acquire(2000L)
    block()
    wakeLock.release()
}

fun TextView.setGradientTest(colorPack: ThemeClockPack = ThemeManager.getCurrentColorPack()) {
    val shader = LinearGradient(
        0f, 0f, width.toFloat() * 1.2f, height.toFloat() * 1.2f, Color.parseColor(colorPack.gradientStart),Color.parseColor(colorPack.gradientEnd),Shader.TileMode.CLAMP)
    paint.shader = shader
//    MainHook.logD("Debug Gradient -> text: $text width: $width height: $height")
}

val OP7DeviceModels = listOf<String>("GM1910", "GM1913", "GM1917", "GM1911", "GM1915", "GM1920")
fun isOP7Pro() = OP7DeviceModels.contains(android.os.Build.MODEL)

/**
 * Only for OP7Pro
 */
@SuppressLint("MissingPermission")
fun Vibrator.simpleTap() {
    setVibratorEffect(1007)
    val effect = VibrationEffect.createWaveform(longArrayOf(0, 11), intArrayOf(0,-1), -1)
    val attr = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build()
    vibrate(effect, attr)
}

/**
 * Only for OP7Pro
 */
fun Vibrator.setVibratorEffect(senceId: Int): Long {
    val TAG = "VibrationHelper2"
    val setVibrationEffect = try {
        Vibrator::class.java.getMethod("setVibratorEffect", Int::class.java)
    } catch (e: NoSuchMethodError) {
        Log.e(TAG, "failed to get method of name 'setVibratorEffect', error= $e")
        return 0L
    }
    try {
        val num = setVibrationEffect.invoke(this, *arrayOf<Any>(senceId)) as? Int
        return num?.toLong() ?: 1L
    } catch (e: IllegalAccessException) {
        Log.e(TAG, "setVibratorEffect# failed to set vibration effect: error= $e")
        return 1L
    } catch (e2: InvocationTargetException) {
        Log.e(TAG, "setVibratorEffect# failed to set vibration effect: error= $e2")
        return 1L
    }
}

fun getDependency(clazz: Class<*>): Any {
    val Dependency = XposedHelpers.findClass("com.android.systemui.Dependency", AndroidAppHelper.currentApplication().classLoader)
    return XposedHelpers.callStaticMethod(Dependency, "get", clazz)
}

fun getVpnStatus() : Boolean{
    val SecurityController = XposedHelpers.findClass("com.android.systemui.statusbar.policy.SecurityController", AndroidAppHelper.currentApplication().classLoader)
    val mSecurityController = getDependency(SecurityController)
    val isVpnEnabled = XposedHelpers.callMethod(mSecurityController, "isVpnEnabled")
    MainHook.logD("Vpn Stat : $isVpnEnabled")
    return isVpnEnabled as Boolean
}

//fun getBlueToothAudioStatus() : Boolean {
//    val BlueToothControllerClass = XposedHelpers.findClass("com.android.systemui.statusbar.policy.BluetoothController", AndroidAppHelper.currentApplication().classLoader)
//    val bluetoothController = getDependency(BlueToothControllerClass)
//    val devices = XposedHelpers.callMethod(bluetoothController, "getConnectedDevices")
//
//
//}