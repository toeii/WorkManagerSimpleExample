package com.toeii.workmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.logging.Handler


class DownloadWork(var context: Context, private var workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {
        //worker execute...
         var result = false

         context.runOnUiThread {
             result = asyncDoWork()
         }

        return if(result){
            Result.success()
        }else{
            Result.failure()
        }

    }

    private fun asyncDoWork(): Boolean {
        var isSuccess = false
        val uri = workerParams.inputData.getString("url")
        Glide.with(context).downloadOnly().load(uri).into(object : SimpleTarget<File>() {
            override fun onResourceReady(
                resource: File,
                transition: com.bumptech.glide.request.transition.Transition<in File>?
            ) {
                try {
                    val folder = context.filesDir.path + File.separator + "download"
                    val appDir = File(folder, "pictures")
                    if (!appDir.exists()) {
                        appDir.mkdirs()
                    }
                    val fileName = System.currentTimeMillis().toString() + ".png"
                    val destFile = File(appDir, fileName)
                    val status = Utils.copy(resource, destFile)
                    if (status) {
                        Utils.addMediaStore(context, destFile, destFile.absolutePath)
                        Toast.makeText(context, "download success", Toast.LENGTH_SHORT).show()
                        isSuccess = true
                    } else {
                        Toast.makeText(context, "download failure", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.stackTrace
                }
            }
        })

        return isSuccess
    }


    override fun onStopped() {
        super.onStopped()
        //worker task stop ...
    }

}