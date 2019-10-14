package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_package.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import java.io.File
import org.apache.commons.io.DirectoryWalker
import org.apache.commons.io.FileUtils
import xyz.velvetmilk.testingtool.tools.encodeHexString
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.io.ByteArrayInputStream


class PackageActivity : AppCompatActivity(), CoroutineScope {

    inner class SizeWalker : DirectoryWalker<Pair<String, Long>>() {

        fun size(startDirectory: String): List<Pair<String, Long>> {
            return size(File(startDirectory))
        }

        fun size(startDirectory: File): List<Pair<String, Long>> {
            val results = mutableListOf<Pair<String, Long>>()
            walk(startDirectory, results)
            return results
        }

        override fun handleFile(
            file: File,
            depth: Int,
            results: MutableCollection<Pair<String, Long>>?
        ) {
            results?.add(Pair(file.absolutePath, file.length()))
        }
    }

    companion object {
        private val TAG = PackageActivity::class.simpleName
        private const val TEST_KEYS_SHA256_FINGERPRINT = "a40da80a59d170caa950cf15c18c454d47a39b26989d8b640ecd745ba71bf5dc"

        fun buildIntent(context: Context): Intent {
            return Intent(context, PackageActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        val packageLocations = mutableListOf<String>()

        launch(Dispatchers.Default) {
            val packageManager = packageManager
            val stringBuilder = StringBuilder()

            stringBuilder.appendln("===== ApplicationInfo =====")
            val appFlags = PackageManager.GET_META_DATA or PackageManager.GET_SHARED_LIBRARY_FILES
            for (apps in packageManager.getInstalledApplications(appFlags)) {
                stringBuilder.appendln(apps.uid)
                stringBuilder.appendln(apps.processName)
                stringBuilder.appendln(apps.className)
                stringBuilder.appendln(apps.deviceProtectedDataDir)
                stringBuilder.append(apps.sourceDir)

                if (apps.sourceDir != apps.publicSourceDir) {
                    Timber.d("sourceDir =/= publicSourceDir")
                    stringBuilder.append(" | ")
                    stringBuilder.appendln(apps.publicSourceDir)
                } else {
                    stringBuilder.appendln()
                }

                // load package locations
                packageLocations.add(apps.sourceDir.substringBeforeLast('/'))

                stringBuilder.appendln()
            }

            stringBuilder.appendln()
            stringBuilder.appendln("===== ModuleInfo =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                for (modules in packageManager.getInstalledModules(0)) {
                    stringBuilder.append(modules.name)
                    stringBuilder.append(" | ")
                    stringBuilder.appendln(modules.packageName)
                    stringBuilder.appendln()
                }
            }

            stringBuilder.appendln()
            stringBuilder.appendln("===== PackageInfo =====")
            var packageFlags =
                PackageManager.GET_ACTIVITIES or PackageManager.GET_CONFIGURATIONS or PackageManager.GET_GIDS or
                        PackageManager.GET_INSTRUMENTATION or PackageManager.GET_INTENT_FILTERS or
                        PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS or PackageManager.GET_PROVIDERS or
                        PackageManager.GET_RECEIVERS or PackageManager.GET_SERVICES or PackageManager.GET_SHARED_LIBRARY_FILES or
                        PackageManager.GET_URI_PERMISSION_PATTERNS
            packageFlags =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageFlags or PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    packageFlags or PackageManager.GET_SIGNATURES
                }

            for (packages in packageManager.getInstalledPackages(packageFlags)) {
                stringBuilder.appendln(packages.gids.toString())
                stringBuilder.appendln(packages.packageName)
                stringBuilder.appendln(packages.sharedUserId)
                stringBuilder.appendln(packages.sharedUserLabel)

                stringBuilder.appendln("===== SigningInfo =====")
                val signatures =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        stringBuilder.appendln(packages.signingInfo.hasMultipleSigners())
                        stringBuilder.appendln(packages.signingInfo.hasPastSigningCertificates())
                        if (packages.signingInfo.hasMultipleSigners()) {
                            packages.signingInfo.apkContentsSigners
                        } else {
                            packages.signingInfo.signingCertificateHistory
                        }
                    } else {
                        packages.signatures
                    }

                stringBuilder.appendln(signatures)
                stringBuilder.appendln()
            }

            launch(Dispatchers.Main) {
                base_view.text = stringBuilder.toString()
            }
        }

        fab.setOnClickListener {
            val count = AtomicInteger(0)
            val completedCount = AtomicInteger(0)
            val builder = StringBuffer()
            val fileList = mutableListOf<Pair<String, Long>>()


            launch(Dispatchers.Default) {
                withContext(Dispatchers.IO) {
                    for (directory in packageLocations) {
                        val files = SizeWalker().size(directory)
                        fileList.addAll(files)
                    }
                }
                fileList.removeAll {
                    // remove apk which is larger than 10mb (check all base.apk strings)
                    // TODO: decompile and read the manifest file
                    !it.first.endsWith(".apk") || it.second > 1024L * 1024L * 10L
                }
                fileList.sortBy { it.second }

                launch(Dispatchers.Main) {
                    progress_bar.max = fileList.lastIndex
                }

                for (i in fileList.indices) {
                    while (count.get() > 10) {
                        delay(10)
                    }
                    Timber.d("LETS GO: " + fileList[i].first)
                    count.incrementAndGet()

                    launch(Dispatchers.IO) {
                        val file = File(fileList[i].first)
                        FileUtils.lineIterator(file, "UTF-8").use {
                            var found = false
                            val results = mutableListOf<String>()
                            while (it.hasNext()) {
                                val line = it.nextLine()
                                if (line.contains("magisk")) {
                                    results.add(line)
                                    found = true
                                    break
                                }
                            }

                            if (found) {
                                builder.appendln(file.absolutePath + " | " + found.toString())
//                                builder.appendln(results.joinToString("\n"))
                            }
                        }
                        count.decrementAndGet()

                        launch(Dispatchers.Main) {
                            progress_bar.progress = completedCount.incrementAndGet()
                            base_view.text = builder.toString()
                        }
                    }
                }
            }
        }

        fab2.setOnClickListener {
            val fileList = mutableListOf<Pair<String, Long>>()
            val stringBuilder = StringBuilder()

            launch(Dispatchers.Default) {
                withContext(Dispatchers.IO) {
                    for (directory in packageLocations) {
                        val files = SizeWalker().size(directory)
                        fileList.addAll(files)
                    }
                }
                fileList.removeAll {
                    // remove apk which is larger than 10mb (check all base.apk strings)
                    // TODO: decompile and read the manifest file
                    !it.first.endsWith(".apk") || it.second > 1024L * 1024L * 10L
                }
                fileList.sortBy { it.second }

                fileList.forEach {
                    stringBuilder.appendln(String.format("%s | %d", it.first, it.second))
                }
                stringBuilder.appendln(fileList.size)

                launch(Dispatchers.Main) {
                    base_view.text = stringBuilder.toString()
                }
            }
        }

        fab3.setOnClickListener {
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(packageManager.isSafeMode)
            for (feature in packageManager.systemAvailableFeatures) {
                stringBuilder.appendln(feature.flags)
                stringBuilder.appendln(feature.name)
                stringBuilder.appendln(feature.version)

                if (feature.name == null) {
                    stringBuilder.appendln(feature.reqGlEsVersion)
                    stringBuilder.appendln(feature.glEsVersion)
                }
            }

            packageManager.systemSharedLibraryNames?.let {
                for (library in it) {
                    stringBuilder.appendln(library)
                }
            }

            stringBuilder.appendln("===== ANDROID O =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                stringBuilder.appendln(packageManager.instantAppCookie)
                stringBuilder.appendln(packageManager.instantAppCookieMaxBytes)
                stringBuilder.appendln(packageManager.isInstantApp)
            }

            stringBuilder.appendln("===== ANDROID P =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                stringBuilder.appendln(packageManager.isPackageSuspended)
            }

            stringBuilder.appendln("===== ANDROID Q =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                stringBuilder.appendln(packageManager.isDeviceUpgrading)
            }

            base_view.text = stringBuilder.toString()
        }

        fab4.setOnClickListener {
            base_view.text = listTestKeyApps().toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        disposer.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getCertificateFingerprint(certArray: ByteArray, algorithm: String): String {
        val input = ByteArrayInputStream(certArray)
        val cert = CertificateFactory.getInstance("X509")
            .generateCertificate(input) as X509Certificate

        val digest = MessageDigest.getInstance(algorithm)
        val hash = digest.digest(cert.encoded)

        return encodeHexString(hash)
    }

    private fun listTestKeyApps(): List<String> {
        val testApps: MutableList<String> = mutableListOf()

        val packageFlags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }

        for (pack in packageManager.getInstalledPackages(packageFlags)) {
            val signatures =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    if (pack.signingInfo.hasMultipleSigners()) {
                        pack.signingInfo.apkContentsSigners
                    } else {
                        pack.signingInfo.signingCertificateHistory
                    }
                } else {
                    pack.signatures
                }

            for (signature in signatures) {
                val fingerprint = getCertificateFingerprint(signature.toByteArray(), "SHA256")
                if (fingerprint == TEST_KEYS_SHA256_FINGERPRINT) {
                    testApps.add(pack.packageName)
                    break
                }
            }
        }

        return testApps
    }
}
