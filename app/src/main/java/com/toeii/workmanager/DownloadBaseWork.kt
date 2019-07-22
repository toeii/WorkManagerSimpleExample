package com.toeii.workmanager

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture



class DownloadBaseWork(appContext: Context, workerParams: WorkerParameters) : ListenableWorker(appContext, workerParams) {

    lateinit var mFuture: SettableFuture<Result>

    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
        mFuture = SettableFuture.create()
        //todo
        mFuture.set(Result.success())

        mFuture.set(Result.failure())
        return mFuture
    }


}