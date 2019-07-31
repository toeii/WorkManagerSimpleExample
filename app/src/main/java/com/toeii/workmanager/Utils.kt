package com.toeii.workmanager

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import android.util.Log

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays

object Utils {

    /**
     * 把文件插入到系统图库
     * @param context
     * @param targetFile 要保存的照片文件
     * @param path  要保存的照片的路径地址
     */
    fun addMediaStore(context: Context, targetFile: File, path: String) {

        val resolver = context.contentResolver
        val newValues = ContentValues(5)
        newValues.put(MediaStore.Images.Media.DISPLAY_NAME, targetFile.name)
        newValues.put(MediaStore.Images.Media.DATA, targetFile.path)
        newValues.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
        newValues.put(MediaStore.Images.Media.SIZE, targetFile.length())
        newValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, newValues)
        MediaScannerConnection.scanFile(context, arrayOf(path), null, null)
    }


    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     * @return 文件是否复制成功
     */
    fun copy(source: File, target: File): Boolean {
        var status = true
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            fileInputStream = FileInputStream(source)
            fileOutputStream = FileOutputStream(target)
            val buffer = ByteArray(1024)
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        } finally {
            try {
                fileInputStream?.close()
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return status
    }

    /**
     * 获取文件夹下所有文件
     */
    fun getFilesAll(path: String): List<File>? {
        val file = File(path)
        val files = file.listFiles()
        if (files == null) {
            Log.e("error", "空目录")
            return null
        }
        return ArrayList(Arrays.asList(*files))
    }


}
