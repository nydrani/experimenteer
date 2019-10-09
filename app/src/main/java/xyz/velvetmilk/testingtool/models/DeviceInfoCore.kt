package xyz.velvetmilk.testingtool.models

import android.content.ContentResolver
import android.content.pm.*
import android.os.Build
import android.os.Debug
import android.provider.Settings
import org.threeten.bp.Instant
import xyz.velvetmilk.testingtool.BuildConfig
import xyz.velvetmilk.testingtool.tools.toBase64

class DeviceInfoCore {

    data class DeviceInfo(val build: BuildInfo,
                          val buildConfig: BuildConfigInfo,
                          val packageInfo: CustomPackageInfo,
//                          val packageList: List<StrippedPackageInfo>,
                          val debug: DebugInfo)

    data class BuildConfigInfo(val applicationId: String,
                               val buildType: String,
                               val debug: Boolean,
                               val flavor: String,
                               val versionCode: Int,
                               val verisionName: String)

    data class CustomPackageInfo(val applicationInfo: ApplicationInfo,
                                 val baseRevisionCode: Int,
//                                 val configPrefences: List<ConfigurationInfo>,
//                                 val featureGroups: List<FeatureGroupInfo>,
                                 val firstInstallTime: Long,
//                                 val gids: List<Int>,
                                 val installLocation: Int,
//                                 val instrumentation: List<InstrumentationInfo>,
                                 val isApex: Boolean,
                                 val lastUpdateTime: Long,
                                 val longVersionCode: Long,
                                 val packageName: String,
//                                 val permissions: List<PermissionInfo>,
//                                 val providers: List<ProviderInfo>,
//                                 val receivers: List<ActivityInfo>,
                                 val reqFeatures: List<FeatureInfo>,
                                 val requestedPermissions: List<String>,
                                 val requestedPermissionsFlags: List<Int>,
//                                 val services: List<ServiceInfo>,
                                 val sharedUserId: String,
                                 val sharedUserLabel: Int,
                                 // base64 of bytearray
                                 val signatures: List<String>,
                                 val splitNames: List<String>,
                                 val splitRevisionCodes: List<Int>,
                                 val versionName: String)

    data class StrippedPackageInfo(val longVersionCode: Long,
                                   val packageName: String,
                                   val signatures: List<String>,
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
//                         val memoryInfo: Debug.MemoryInfo,
                         val nativeHeapAllocatedSize: Long,
                         val nativeHeapFreeSize: Long,
                         val nativeHeapSize: Long,
                         val pss: Long,
//                         val runtimeStats: Map<String, String>,
                         val isDebuggerConnected: Boolean)

    companion object {
        private fun generateDebugInfo(): DebugInfo {
            val memoryInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memoryInfo)

            return DebugInfo(
                Debug.getLoadedClassCount(),
//                memoryInfo,
                Debug.getNativeHeapAllocatedSize(),
                Debug.getNativeHeapFreeSize(),
                Debug.getNativeHeapSize(),
                Debug.getPss(),
//                Debug.getRuntimeStats(),
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
                generateCustomPackageInfo(packageInfo),
//                generateStrippedPackageListInfo(packageManager),
                generateDebugInfo()
            )
        }

        private fun generateCustomPackageInfo(packageInfo: PackageInfo): CustomPackageInfo {
            val apex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                packageInfo.isApex
            } else {
                false
            }

            val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }

            val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (packageInfo.signingInfo.hasMultipleSigners()) {
                    packageInfo.signingInfo.apkContentsSigners
                } else {
                    packageInfo.signingInfo.signingCertificateHistory
                }
            } else {
                packageInfo.signatures
            }

            val signatureList: List<String> = signatures.map {
                it.toByteArray().toBase64()
            }

            return CustomPackageInfo(packageInfo.applicationInfo,
                packageInfo.baseRevisionCode,
//                packageInfo.configPreferences?.toList() ?: listOf(),
//                packageInfo.featureGroups?.toList() ?: listOf(),
                packageInfo.firstInstallTime,
//                packageInfo.gids.toList(),
                packageInfo.installLocation,
//                packageInfo.instrumentation?.toList() ?: listOf(),
                apex,
                packageInfo.lastUpdateTime,
                longVersionCode,
                packageInfo.packageName,
//                packageInfo.permissions?.toList() ?: listOf(),
//                packageInfo.providers?.toList() ?: listOf(),
//                packageInfo.receivers?.toList() ?: listOf(),
                packageInfo.reqFeatures?.toList() ?: listOf(),
                packageInfo.requestedPermissions?.toList() ?: listOf(),
                packageInfo.requestedPermissionsFlags?.toList() ?: listOf(),
//                packageInfo.services?.toList() ?: listOf(),
                packageInfo.sharedUserId ?: "",
                packageInfo.sharedUserLabel,
                signatureList,
                packageInfo.splitNames?.toList() ?: listOf(),
                packageInfo.splitRevisionCodes?.toList() ?: listOf(),
                packageInfo.versionName)
        }

        private fun generateStrippedPackageInfo(packageInfo: PackageInfo): StrippedPackageInfo {
            val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }

            val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (packageInfo.signingInfo.hasMultipleSigners()) {
                    packageInfo.signingInfo.apkContentsSigners
                } else {
                    packageInfo.signingInfo.signingCertificateHistory
                }
            } else {
                packageInfo.signatures
            }

            val signatureList: List<String> = signatures.map {
                it.toByteArray().toBase64()
            }

            return StrippedPackageInfo(
                longVersionCode,
                packageInfo.packageName,
                signatureList,
                packageInfo.versionName
            )
        }

        private fun generateStrippedPackageListInfo(packageManager: PackageManager): List<StrippedPackageInfo> {
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

            val packageList = mutableListOf<StrippedPackageInfo>()

            val packages = packageManager.getInstalledPackages(flags)
            for (pack in packages) {
                packageList.add(generateStrippedPackageInfo(pack))
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
