package com.example.update

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.example.BuildConfig
import kotlin.concurrent.thread

/** Checks for a new version in the background every few hours and sends a notification. */
class UpdateJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        thread {
            val latest = UpdateChecker.fetchLatestVersion()
            if (latest != null && UpdateChecker.isNewer(latest, BuildConfig.VERSION_NAME)) {
                UpdateNotifier.notifyNewVersion(applicationContext, latest)
            }
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = true

    companion object {
        private const val JOB_ID = 4401

        fun schedule(context: Context) {
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            if (scheduler.getPendingJob(JOB_ID) != null) return
            val job = JobInfo.Builder(JOB_ID, ComponentName(context, UpdateJobService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(6 * 60 * 60 * 1000L)
                .setPersisted(true)
                .build()
            scheduler.schedule(job)
        }
    }
}
