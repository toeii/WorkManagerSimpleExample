package com.pingrex.preview.listenner

import android.graphics.Bitmap

/**
 * @author Pinger
 * @since 18-8-1 下午3:29
 * 加载图片的监听回调
 */

interface ImagePreviewLoadListener {


    /**
     * 加载成功
     * @param bitmap 加载成功返回的bitmap
     */
    fun onLoadSuccess(bitmap: Bitmap?)

    /**
     * 加载失败
     * @param msg 失败原因
     */
    fun onLoadFail(msg: String?)
}