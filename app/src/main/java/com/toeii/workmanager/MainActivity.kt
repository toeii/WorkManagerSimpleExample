package com.toeii.workmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.example.background.workers.UploadWorker
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val iconDownload = findViewById<ImageView>(R.id.iv_icon)
        val btnSave = findViewById<Button>(R.id.btn_save)
        val btnLook = findViewById<Button>(R.id.btn_look)

        val uri = Images.randomStockImage()
        Glide.with(this).load(uri).into(iconDownload)

        btnSave.setOnClickListener {

            // WorkManager


            // BaseWorkManager


        }

        btnLook.setOnClickListener {



        }

    }

}
