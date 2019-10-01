package xyz.velvetmilk.testingtool.models

import android.content.ContentResolver
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.provider.Settings
import org.threeten.bp.Instant
import xyz.velvetmilk.testingtool.BuildConfig

data class DeviceInfo(val build: BuildInfo,
                      val buildConfig: BuildConfigInfo,
                      val packageInfo: PackageInfo,
                      val debug: DebugInfo)

data class BuildConfigInfo(val applicationId: String,
                           val buildType: String,
                           val debug: Boolean,
                           val flavor: String,
                           val versionCode: Int,
                           val verisionName: String)

data class BuildInfo(val board: String,
                     val bootloader: String,
                     val brand: String,
                     val device: String,
                     val display: String,
                     val fingerprint: String,
                     val hardware: String,
                     val host: String,
                     val id: String,
                     val manufacturer: String,
                     val model: String,
                     val product: String,
                     val abis: List<String>,
                     val tags: String,
                     val time: Instant,
                     val type: String,
                     val user: String,
                     val radioVersion: String,
                     val partitions: List<PartitionInfo>,
                     val serial: String,
                     val androidId: String)

data class PartitionInfo(val buildTime: Instant,
                         val fingerprint: String,
                         val name: String)

data class DebugInfo(val runtimeStats: Map<String, String>,
                     val pss: Long,
                     val nativeHeapAllocatedSize: Long,
                     val nativeHeapFreeSize: Long,
                     val nativeHeapSize: Long,
                     val loadedClassCount: Int,
                     val isDebuggerConnected: Boolean)


class DeviceInfoCore {


    companion object {
        fun generateDebugInfo(): DebugInfo {
            return DebugInfo(
                Debug.getRuntimeStats(),
                Debug.getPss(),
                Debug.getNativeHeapAllocatedSize(),
                Debug.getNativeHeapFreeSize(),
                Debug.getNativeHeapSize(),
                Debug.getLoadedClassCount(),
                Debug.isDebuggerConnected()
            )
        }

        @SuppressWarnings("HardwareIds")
        fun generateBuildInfo(contentResolver: ContentResolver): BuildInfo {
            val serial: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Build.getSerial()
                } catch (e: SecurityException) {
                    // whatever
                    ""
                }
            } else {
                Build.SERIAL
            }

            val partitions: List<PartitionInfo> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Build.getFingerprintedPartitions().map {
                        PartitionInfo(Instant.ofEpochMilli(it.buildTimeMillis),
                            it.fingerprint,
                            it.name)
                    }
                } else {
                    listOf()
                }

            return BuildInfo(
                Build.BOARD,
                Build.BOOTLOADER,
                Build.BRAND,
                Build.DEVICE,
                Build.DISPLAY,
                Build.FINGERPRINT,
                Build.HARDWARE,
                Build.HOST,
                Build.ID,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.PRODUCT,
                Build.SUPPORTED_ABIS.toList(),
                Build.TAGS,
                Instant.ofEpochMilli(Build.TIME),
                Build.TYPE,
                Build.USER,
                Build.getRadioVersion(),
                partitions,
                serial,
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            )
        }

        fun generateDeviceInfo(
            contentResolver: ContentResolver,
            packageManager: PackageManager,
            packageName: String
        ): DeviceInfo {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            return DeviceInfo(
                generateBuildInfo(contentResolver),
                generateBuildConfigInfo(),
                packageInfo,
                generateDebugInfo()
            )
        }

        fun generateBuildConfigInfo(): BuildConfigInfo {
            return BuildConfigInfo(
                BuildConfig.APPLICATION_ID,
                BuildConfig.BUILD_TYPE,
                BuildConfig.DEBUG,
                BuildConfig.FLAVOR,
                BuildConfig.VERSION_CODE,
                BuildConfig.VERSION_NAME
            )
        }
    }
}
