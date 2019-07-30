package xyz.velvetmilk.testingtool.tools

import android.app.ActivityManager
import java.lang.NumberFormatException
import android.content.Context
import android.content.SharedPreferences
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.zip.ZipFile
import android.telephony.TelephonyManager
import xyz.velvetmilk.testingtool.R
import javax.crypto.Mac
import javax.crypto.SecretKey
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import java.io.*

class AntiDebugging {

    companion object {
        private const val PREFERENCE_HMACSHA256_KEY = "PREFERENCE_HMACSHA256_KEY"

        private val known_numbers = arrayOf(
            "15555215554", // Default emulator phone numbers + VirusTotal
            "15555215556",
            "15555215558",
            "15555215560",
            "15555215562",
            "15555215564",
            "15555215566",
            "15555215568",
            "15555215570",
            "15555215572",
            "15555215574",
            "15555215576",
            "15555215578",
            "15555215580",
            "15555215582",
            "15555215584"
        )
        private val known_device_ids = arrayOf(
            "000000000000000", // Default emulator id
            "e21833235b6eef10", // VirusTotal id
            "012345678912345"
        )
        private val known_imsi_ids = arrayOf(
            "310260000000000" // Default imsi id
        )
        private val known_pipes = arrayOf("/dev/socket/qemud", "/dev/qemu_pipe")
        private val known_files = arrayOf(
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace", "/system/bin/qemu-props"
        )
        private val known_geny_files = arrayOf("/dev/socket/genyd", "/dev/socket/baseband_genyd")
        private val known_qemu_drivers = arrayOf("goldfish")
        private val known_props = arrayOf(
            Pair("init.svc.qemud", null),
            Pair("init.svc.qemu-props", null),
            Pair("qemu.hw.mainkeys", null),
            Pair("qemu.sf.fake_camera", null),
            Pair("qemu.sf.lcd_density", null),
            Pair("ro.bootloader", "unknown"),
            Pair("ro.bootmode", "unknown"),
            Pair("ro.hardware", "goldfish"),
            Pair("ro.kernel.android.qemud", null),
            Pair("ro.kernel.qemu.gles", null),
            Pair("ro.kernel.qemu", "1"),
            Pair("ro.product.device", "generic"),
            Pair("ro.product.model", "sdk"),
            Pair("ro.product.name", "sdk"),
            Pair("ro.serialno", null)
        )


        fun hasTracerPid(): Boolean {
            val tracerpid = "TracerPid:"

            BufferedReader(FileReader("/proc/self/status")).useLines {
                for (line in it) {
                    if (line.length < tracerpid.length) {
                        continue
                    }

                    if (!line.startsWith(tracerpid)) {
                        continue
                    }

                    val res = line.split("\\W+".toRegex())
                    try {
                        if (res[1].toInt() > 0) {
                            return true
                        }
                    } catch (e: NumberFormatException) {
                        return false
                    }
                }
            }
            return false
        }

        fun isUserAMonkey() {
            ActivityManager.isUserAMonkey()
        }

        fun isRunningInADeviceFarm(): Boolean {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ActivityManager.isRunningInUserTestHarness()
            } else {
                ActivityManager.isRunningInTestHarness()
            }
        }

        fun isOperatorNameAndroid(context: Context): Boolean {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val operatorName = telephonyManager.networkOperatorName
            return operatorName == "android"
        }

        @Throws(IOException::class)
        fun sha256Check(context: Context): Boolean {
            // required dex crc value stored as a text string.
            // it could be any invisible layout element
            val dexHash = context.getString(R.string.dex_sha256_base64)

            val zf = ZipFile(context.packageCodePath)
            val ze = zf.getEntry("classes.dex")
            val inputStream = zf.getInputStream(ze)

            val digest = MessageDigest.getInstance("SHA-256")
            val dis = DigestInputStream(inputStream, digest)

            return dis.messageDigest.digest().toBase64() == dexHash
        }

        @Throws(IOException::class)
        fun hmacsha256Check(key: SecretKey, sharedPreferences: SharedPreferences): Boolean {
            val hmacValue = sharedPreferences.getString(PREFERENCE_HMACSHA256_KEY, null)
            // early exit if hmac doesnt exist
            hmacValue ?: return false

            val origMap = sharedPreferences.all

            val gson = Gson()
            val json = gson.toJson(origMap)
            val mapCopy: MutableMap<String, Any> = gson.fromJson(json, object : TypeToken<Map<String, Any>>() {}.type) as MutableMap<String, Any>
            mapCopy.remove(PREFERENCE_HMACSHA256_KEY)

            val bos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(bos)
            oos.writeObject(mapCopy)
            val mapBytes = bos.toByteArray()

            val mac = Mac.getInstance("HmacSHA256")
            mac.init(key)
            val preferencesMac = mac.doFinal(mapBytes)

            return hmacValue.fromBase64().contentEquals(preferencesMac)
        }

        @Throws(IOException::class)
        fun hmacsha256Generate(key: SecretKey, sharedPreferences: SharedPreferences) {
            val bos = ByteArrayOutputStream()
            val oos = ObjectOutputStream(bos)

            oos.writeObject(sharedPreferences.all)
            val mapBytes = bos.toByteArray()

            val mac = Mac.getInstance("HmacSHA256")
            mac.init(key)
            val preferencesMac = mac.doFinal(mapBytes)

            sharedPreferences.edit().putString(PREFERENCE_HMACSHA256_KEY, preferencesMac.toBase64()).apply()
        }
    }
}
