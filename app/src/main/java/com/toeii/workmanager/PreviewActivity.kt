package com.toeii.workmanager

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pingrex.preview.ImagePreview
import com.pingrex.preview.wrapper.ImageEntity
import com.pingrex.preview.wrapper.ImagePreviewAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_img_preview)

        val appDir = filesDir.path + File.separator + "download" + File.separator + "pictures"

        doAsync {

            val files =  Utils.getFilesAll(appDir)
            val images: MutableList<ImageEntity> = ArrayList()
            for(file in files!!){
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val imageEntity = ImageEntity()
                imageEntity.imageUrl = file.absolutePath
                imageEntity.imageHeight = bitmap.height
                imageEntity.imageWidth = bitmap.width
                images.add(imageEntity)
            }

            uiThread {
                findViewById<ImagePreview>(R.id.imagePreview).setAdapter(ImagePreviewAdapter(images))
            }

        }

    }


}