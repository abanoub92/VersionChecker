package com.abanoub.versionchecker

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.Callable

class VersionChecker private constructor(private val activity: Activity) {

    private val pInfo: PackageInfo =
        activity.packageManager.getPackageInfo(activity.packageName, 0)

    private val currentVersion: String = pInfo.versionName
    private val appPackageName: String = activity.packageName

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: VersionChecker? = null

        fun getInstance(activity: Activity) =
            instance ?: synchronized(this) {
                instance ?: VersionChecker(activity).also { instance = it }
            }
    }

    @SuppressLint("CheckResult")
    fun check() {
        Callable {
            val doc: Document =
                Jsoup.connect("https://play.google.com/store/apps/details?id=$appPackageName")
                    .get()
            doc.getElementsByAttributeValue("itemprop", "softwareVersion")
        }.let {
            Observable.fromCallable(it).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s ->
                    if (s.text() != currentVersion) if (!activity.isFinishing) //to avoid crashing when activiy is not visible
                        showUpdate().create().show()
                }
        }
    }

    private fun showUpdate(): AlertDialog.Builder {
        return AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.version_checker_title))
            .setMessage(activity.getString(R.string.version_checker_desc))
            .setCancelable(false)
            .setPositiveButton(activity.getString(R.string.version_checker_ok)
            ) { _, _ ->
                try {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (ignored: ActivityNotFoundException) {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                    ignored.printStackTrace()
                }
            }
    }

}