package xyz.velvetmilk.testingtool.jni

import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import xyz.velvetmilk.testingtool.tools.fromBase64
import xyz.velvetmilk.testingtool.tools.toBase64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@SuppressLint("UnsafeDynamicallyLoadedCode")
class ExternalJniLib @Throws(UnsatisfiedLinkError::class, GeneralSecurityException::class) constructor(context: Context, type: LibraryType) {

    enum class LibraryType {
        INTERNAL,
        EXTERNAL;
    }

    companion object {
        private val TAG = ExternalJniLib::class.simpleName

        private const val AES_ALGORITHM = "AES"
        private const val AES_CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding"
        private const val AES_KEY_ALIAS = "externalaeskey"
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val EXTERNAL_LIB_IV_KEY = "EXTERNAL_LIB_IV"

        // NOTE: last 32 bytes are [key, iv]
        fun storeEncFile(context: Context, stream: InputStream) {
            val encBytes = stream.readBytes()

            // store file
            File(context.filesDir, "libexternal.enc")
                .outputStream()
                .use { it.write(encBytes, 0, encBytes.size - 32) }

            // extract key and iv
            val aesKey = encBytes.copyOfRange(encBytes.size - 32, encBytes.size - 16)
            val iv = encBytes.copyOfRange(encBytes.size - 16, encBytes.size)

            // store aes key
            val store = KeyStore.getInstance(KEYSTORE_TYPE).apply { this.load(null) }
            val secretKeyFactory = SecretKeyFactory.getInstance(AES_ALGORITHM)

            val aesKeyProtection =
                KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()

            val secretKeySpec = SecretKeySpec(aesKey, KeyProperties.KEY_ALGORITHM_AES)
            val secretKey = secretKeyFactory.generateSecret(secretKeySpec)
            store.setEntry(AES_KEY_ALIAS, KeyStore.SecretKeyEntry(secretKey), aesKeyProtection)

            // store iv in sharedpreferences
            val sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(EXTERNAL_LIB_IV_KEY, iv.toBase64()).apply()
        }
    }

    init {
        // NOTE: this will only load one, not possible to unload a library with inbuilt classloader
        if (type == LibraryType.INTERNAL) {
            System.loadLibrary("external")
        } else {
            // unencrypt and load into the system (delete file on loading into memory)
            val uncFile = unencryptFile(context)
            System.load(uncFile.absolutePath)
            uncFile.delete()
        }
    }

    external fun ping(): Boolean

    @Throws(GeneralSecurityException::class)
    private fun unencryptFile(context: Context): File {
        val sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val iv = (sharedPreferences.getString(EXTERNAL_LIB_IV_KEY, null) ?: "").fromBase64()

        val store = KeyStore.getInstance(KEYSTORE_TYPE).apply { this.load(null) }
        val secretKeyEntry = store.getEntry(AES_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val inputFile = File(context.filesDir, "libexternal.enc")

        FileInputStream(inputFile).use { inStream ->
            val inputBytes = inStream.readBytes()
            val cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val outputBytes = cipher.doFinal(inputBytes)

            val outputFile = File.createTempFile("libexternal-", ".unc")
            FileOutputStream(outputFile).use { outStream ->
                outStream.write(outputBytes)
            }

            return outputFile
        }
    }
}
