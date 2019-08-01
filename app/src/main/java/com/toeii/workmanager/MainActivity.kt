package com.toeii.workmanager

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.bumptech.glide.Glide
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

     private lateinit var uri:String

    @RequiresApi(Build.VERSION_CODES.DONUT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val iconDownload = findViewById<ImageView>(R.id.iv_icon)
        val btnSave = findViewById<Button>(R.id.btn_save)
        val btnLook = findViewById<Button>(R.id.btn_look)

        uri = Constants.randomStockImage()
        Glide.with(this).load(uri).into(iconDownload)

        btnSave.setOnClickListener {

            val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    201
                )
            } else {
                readDataExternal(uri)
            }

        }

        btnLook.setOnClickListener {
            openDataExternal()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            201 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readDataExternal(uri)
            }else -> {

            }
        }
    }

    private fun readDataExternal(uri:String) {

        // WorkManager
        val data = Data.Builder().putString("url",uri).build()

        val myConstraints = Constraints.Builder()
//                .setRequiresDeviceIdle(true)//指定{@link WorkRequest}运行时设备是否为空闲
//                .setRequiresCharging(true)//指定要运行的{@link WorkRequest}是否应该插入设备
//                .setRequiredNetworkType(NetworkType.NOT_ROAMING)
//                .setRequiresBatteryNotLow(true)//指定设备电池是否不应低于临界阈值
//                .setRequiresCharging(true)//网络状态
//                .setRequiresDeviceIdle(true)//指定{@link WorkRequest}运行时设备是否为空闲
//                .setRequiresStorageNotLow(true)//指定设备可用存储是否不应低于临界阈值
//                .addContentUriTrigger(Uri.parse(uri),false)//指定内容{@link android.net.Uri}时是否应该运行{@link WorkRequest}更新
            .build()

        val requestOneTimeWork = OneTimeWorkRequestBuilder<DownloadWork>()
            .setInputData(data)
            .setConstraints(myConstraints)
            .build()

        // added enqueue one time work
        WorkManager.getInstance(this@MainActivity).enqueue(requestOneTimeWork)
        // cancel work from id
//            WorkManager.getInstance(this@MainActivity).cancelWorkById(requestOneTimeWork.id)

        //query work result
        val liveData: LiveData<WorkInfo> = WorkManager.getInstance(this@MainActivity).getWorkInfoByIdLiveData(requestOneTimeWork.id)
        liveData.observe(this, Observer {
            if (it?.state!!.isFinished) {
                if(liveData.value?.state == WorkInfo.State.SUCCEEDED){
                    //success
                    println("download success")
                }else{
                    //other
                    println("download error")
                }
            }
        })

        /*
            // added enqueue periodic work
            val requestPeriodicWork = PeriodicWorkRequestBuilder<DownloadWork>(60, TimeUnit.SECONDS)
                 .setInputData(data)
                 .setConstraints(myConstraints)
                 .build()

             WorkManager.getInstance(this@MainActivity).enqueue(requestPeriodicWork)
         */
    }


    private fun openDataExternal() {
        startActivity(Intent(this,PreviewActivity::class.java))
    }

}
