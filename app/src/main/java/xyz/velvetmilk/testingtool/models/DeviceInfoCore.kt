package xyz.velvetmilk.testingtool.models

import android.content.ContentResolver
import android.content.pm.*
import android.os.Build
import android.os.Debug
import android.provider.Settings
import org.threeten.bp.Instant
import xyz.velvetmilk.testingtool.BuildConfig

class DeviceInfoCore {

    data class DeviceInfo(val build: BuildInfo,
                          val buildConfig: BuildConfigInfo,
                          val packageInfo: PackageInfo,
                          val packageList: List<CustomPackageInfo>,
                          val debug: DebugInfo)

    data class BuildConfigInfo(val applicationId: String,
                               val buildType: String,
                               val debug: Boolean,
                               val flavor: String,
                               val versionCode: Int,
                               val verisionName: String)

    data class CustomPackageInfo(val applicationInfo: ApplicationInfo,
                                 val baseRevisionCode: Int,
                                 val configPrefences: List<ConfigurationInfo>,
                                 val featureGroups: List<FeatureGroupInfo>,
                                 val firstInstallTime: Long,
                                 val gids: List<Int>,
                                 val installLocation: Int,
                                 val instrumentation: List<InstrumentationInfo>,
                                 val isApex: Boolean,
                                 val lastUpdateTime: Long,
                                 val longVersionCode: Long,
                                 val packageName: String,
                                 val permissions: List<PermissionInfo>,
                                 val providers: List<ProviderInfo>,
                                 val receivers: List<ActivityInfo>,
                                 val reqFeatures: List<FeatureInfo>,
                                 val requestedPermissions: List<String>,
                                 val requestedPermissionsFlags: List<Int>,
                                 val services: List<ServiceInfo>,
                                 val sharedUserId: String,
                                 val sharedUserLabel: Int,
                                 val signatures: List<Signature>,
                                 val splitNames: List<String>,
                                 val splitRevisionCodes: List<Int>,
                                 val versionName: String)

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
                         val androidId: String,
                         val versionInfo: BuildVersionInfo)

    data class BuildVersionInfo(val baseOS: String,
                                val codename: String,
                                val incremental: String,
                                val previewSDKInt: Int,
                                val release: String,
                                val SDKInt: Int,
                                val securityPatch: String)

    data class PartitionInfo(val buildTime: Instant,
                             val fingerprint: String,
                             val name: String)

    data class DebugInfo(val loadedClassCount: Int,
                         val memoryInfo: Debug.MemoryInfo,
                         val nativeHeapAllocatedSize: Long,
                         val nativeHeapFreeSize: Long,
                         val nativeHeapSize: Long,
                         val pss: Long,
                         val runtimeStats: Map<String, String>,
                         val isDebuggerConnected: Boolean)

    companion object {

        private fun generateDebugInfo(): DebugInfo {
            val memoryInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memoryInfo)

            return DebugInfo(
                Debug.getLoadedClassCount(),
                memoryInfo,
                Debug.getNativeHeapAllocatedSize(),
                Debug.getNativeHeapFreeSize(),
                Debug.getNativeHeapSize(),
                Debug.getPss(),
                Debug.getRuntimeStats(),
                Debug.isDebuggerConnected()
            )
        }

        @SuppressWarnings("HardwareIds")
        private fun generateBuildInfo(contentResolver: ContentResolver): BuildInfo {
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
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
                generateBuildVersionInfo()
            )
        }

        private fun generateBuildVersionInfo(): BuildVersionInfo {
            return BuildVersionInfo(Build.VERSION.BASE_OS,
                Build.VERSION.CODENAME,
                Build.VERSION.INCREMENTAL,
                Build.VERSION.PREVIEW_SDK_INT,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.VERSION.SECURITY_PATCH)
        }

        fun generateDeviceInfo(
            contentResolver: ContentResolver,
            packageManager: PackageManager,
            packageName: String
        ): DeviceInfo {
            var flags = PackageManager.GET_ACTIVITIES or PackageManager.GET_CONFIGURATIONS or
                    PackageManager.GET_GIDS or PackageManager.GET_INSTRUMENTATION or
                    PackageManager.GET_INTENT_FILTERS or PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_PROVIDERS or PackageManager.GET_RECEIVERS or
                    PackageManager.GET_SERVICES

            flags = flags or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                PackageManager.GET_SIGNATURES
            }

            // componentinfo
            flags = flags or PackageManager.GET_META_DATA

            // applicationinfo
            flags = flags or PackageManager.GET_SHARED_LIBRARY_FILES

            // providerinfo
            flags = flags or PackageManager.GET_URI_PERMISSION_PATTERNS

            val packageInfo = packageManager.getPackageInfo(packageName, flags)
            return DeviceInfo(
                generateBuildInfo(contentResolver),
                generateBuildConfigInfo(),
                packageInfo,
                generateCustomPackageInfo(packageManager),
                generateDebugInfo()
            )
        }

        private fun generateCustomPackageInfo(packageManager: PackageManager): List<CustomPackageInfo> {
            var flags = PackageManager.GET_ACTIVITIES or PackageManager.GET_CONFIGURATIONS or
                    PackageManager.GET_GIDS or PackageManager.GET_INSTRUMENTATION or
                    PackageManager.GET_INTENT_FILTERS or PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_PROVIDERS or PackageManager.GET_RECEIVERS or
                    PackageManager.GET_SERVICES

            flags = flags or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                PackageManager.GET_SIGNATURES
            }

            // componentinfo
            flags = flags or PackageManager.GET_META_DATA

            // applicationinfo
            flags = flags or PackageManager.GET_SHARED_LIBRARY_FILES

            // providerinfo
            flags = flags or PackageManager.GET_URI_PERMISSION_PATTERNS

            val packageList = mutableListOf<CustomPackageInfo>()

            val packages = packageManager.getInstalledPackages(flags)
            for (pack in packages) {
                val apex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    pack.isApex
                } else {
                    false
                }

                val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pack.longVersionCode
                } else {
                    pack.versionCode.toLong()
                }

                val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (pack.signingInfo.hasMultipleSigners()) {
                        pack.signingInfo.apkContentsSigners
                    } else {
                        pack.signingInfo.signingCertificateHistory
                    }
                } else {
                    pack.signatures
                }

                packageList.add(CustomPackageInfo(pack.applicationInfo,
                    pack.baseRevisionCode,
                    pack.configPreferences?.toList() ?: listOf(),
                    pack.featureGroups?.toList() ?: listOf(),
                    pack.firstInstallTime,
                    pack.gids.toList(),
                    pack.installLocation,
                    pack.instrumentation?.toList() ?: listOf(),
                    apex,
                    pack.lastUpdateTime,
                    longVersionCode,
                    pack.packageName,
                    pack.permissions?.toList() ?: listOf(),
                    pack.providers?.toList() ?: listOf(),
                    pack.receivers?.toList() ?: listOf(),
                    pack.reqFeatures?.toList() ?: listOf(),
                    pack.requestedPermissions?.toList() ?: listOf(),
                    pack.requestedPermissionsFlags?.toList() ?: listOf(),
                    pack.services?.toList() ?: listOf(),
                    pack?.sharedUserId ?: "",
                    pack.sharedUserLabel,
                    signatures.toList(),
                    pack.splitNames?.toList() ?: listOf(),
                    pack.splitRevisionCodes?.toList() ?: listOf(),
                    pack.versionName)
                )
            }

            return packageList
        }

        private fun generateBuildConfigInfo(): BuildConfigInfo {
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
