package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_package.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.StringBuilder
import kotlin.coroutines.CoroutineContext
import java.io.File
import org.apache.commons.io.DirectoryWalker
import org.apache.commons.io.FileUtils
import java.util.concurrent.atomic.AtomicInteger


class PackageActivity : AppCompatActivity(), CoroutineScope {

//    inner class FileWalker {
//        fun walk(root: String): List<File> {
//            return walk(File(root))
//        }
//
//        fun walk(root: File): List<File> {
//            val list = root.listFiles()
//            val folderList = mutableListOf<File>()
//
//            if (list == null) {
//                return folderList
//            }
//
//            for (f in list) {
//                if (f.isDirectory) {
//                    folderList.addAll(walk(f))
//                } else {
//                    folderList.add(f)
//                }
//            }
//
//            return folderList
//        }
//    }

    inner class GrepWalker : DirectoryWalker<String>() {

        fun grep(startDirectory: String): List<String> {
            return grep(File(startDirectory))
        }

        fun grep(startDirectory: File): List<String> {
            val results = mutableListOf<String>()
            walk(startDirectory, results)
            return results
        }

        override fun handleFile(file: File, depth: Int, results: MutableCollection<String>?) {
            launch(Dispatchers.Main) {
                Toast.makeText(this@PackageActivity, "Looking at: " + file.absoluteFile + " | " + file.length(), Toast.LENGTH_SHORT).show()
            }

            FileUtils.lineIterator(file, "UTF-8").use {
                while (it.hasNext()) {
                    val line = it.nextLine()
                    if (line.matches("magisk".toRegex())) {
                        results?.add(file.absolutePath)
                    }
                }
            }
        }
    }

    inner class SizeWalker : DirectoryWalker<Pair<String, Long>>() {

        fun size(startDirectory: String): List<Pair<String, Long>> {
            return size(File(startDirectory))
        }

        fun size(startDirectory: File): List<Pair<String, Long>> {
            val results = mutableListOf<Pair<String, Long>>()
            walk(startDirectory, results)
            return results
        }

        override fun handleFile(file: File, depth: Int, results: MutableCollection<Pair<String, Long>>?) {
            results?.add(Pair(file.absolutePath, file.length()))
        }
    }

    companion object {
        private val TAG = PackageActivity::class.simpleName

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

        val stringBuilder = StringBuilder()
        val packageLocations = mutableListOf<String>()
        val allFiles = mutableListOf<Pair<String, Long>>()

        launch(Dispatchers.Default) {
            val packageManager = packageManager

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
            }

            stringBuilder.appendln()
            stringBuilder.appendln("===== ModuleInfo =====")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                for (modules in packageManager.getInstalledModules(0)) {
                    stringBuilder.append(modules.name)
                    stringBuilder.append(" | ")
                    stringBuilder.appendln(modules.packageName)
                }
            }

            stringBuilder.appendln()
            stringBuilder.appendln("===== PackageInfo =====")
            var packageFlags = PackageManager.GET_ACTIVITIES or PackageManager.GET_CONFIGURATIONS or PackageManager.GET_GIDS or
                    PackageManager.GET_INSTRUMENTATION or PackageManager.GET_INTENT_FILTERS or
                    PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS or PackageManager.GET_PROVIDERS or
                    PackageManager.GET_RECEIVERS or PackageManager.GET_SERVICES or PackageManager.GET_SHARED_LIBRARY_FILES or
                    PackageManager.GET_URI_PERMISSION_PATTERNS
            packageFlags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageFlags or PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                packageFlags or PackageManager.GET_SIGNATURES
            }

            for (packages in packageManager.getInstalledPackages(packageFlags)) {
                stringBuilder.appendln(packages.gids.toString())
                stringBuilder.appendln(packages.packageName)
                stringBuilder.appendln(packages.sharedUserId)
                stringBuilder.appendln(packages.sharedUserLabel)

                /*
                stringBuilder.appendln("===== SigningInfo =====")
                val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
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
                for (signature in signatures) {
                    stringBuilder.appendln(signature.toCharsString())
                }
                */
            }

            launch(Dispatchers.Main) {
                base_view.text = stringBuilder.toString()
            }
        }

        fab.setOnClickListener {
            val count = AtomicInteger(0)
            val builder = StringBuffer()

            launch(Dispatchers.Default) {
                for (i in allFiles.indices) {
                    while (count.get() > 100) {
                        delay(10)
                    }
                    Timber.d("LETS GO: " + allFiles[i].first)
                    count.incrementAndGet()

                    launch(Dispatchers.Main) {
                        progress_bar.progress = i
                    }

                    launch(Dispatchers.IO) {
                        val file = File(allFiles[i].first)
                        FileUtils.lineIterator(file, "UTF-8").use {
                            var found = false
                            val results = mutableListOf<String>()
                            while (it.hasNext()) {
                                val line = it.nextLine()
                                if (line.contains("magisk")) {
                                    results.add(line)
                                    found = true
                                }
                            }

                            if (found) {
                                builder.appendln(file.absolutePath + " | " + found.toString())
                                builder.appendln(results.toString())
                            }
                        }
                        count.decrementAndGet()

                        launch(Dispatchers.Main) {
                            base_view.text = builder.toString()
                        }
                    }
                }
            }


            base_view.text = builder.toString()
        }

        fab2.setOnClickListener {
            launch(Dispatchers.Default) {
                allFiles.clear()
                for (directory in packageLocations) {
                    withContext(Dispatchers.IO) {
                        val files = SizeWalker().size(directory)
                        allFiles.addAll(files)
                    }
                }
                allFiles.sortBy { it.second }

                launch(Dispatchers.Main) {
                    base_view.text = allFiles.toString() + "\n" + allFiles.size.toString()
                    progress_bar.max = allFiles.lastIndex
                }
            }
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
}
