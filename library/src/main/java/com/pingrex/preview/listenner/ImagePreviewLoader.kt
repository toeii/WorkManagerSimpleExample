package com.pingrex.preview.listenner

import android.graphics.Bitmap
import android.widget.ImageView

/**
 * @author Pinger
 * @since 18-8-1 下午3:27
 * 加载图片的接口
 */

interface ImagePreviewLoader {


    /**
     * 加载图片
     *
     * @param any       图片地址
     * @param imageView 需要展示图片的ImageView
     * @param listener 图片加载的监听
     */
    fun onLoadPreviewImage(any: Any?, imageView: ImageView?, listener: ImagePreviewLoadListener? = null)

    /**
     * 加载图片，生成bitmap，在回调里处理数据
     */
    fun onLoadPreviewImage(any: Any?, listener: ImagePreviewLoadListener?) {}

    /**
     * @param any 图片的地址
     * @return 当前框架的本地缓存图片
     */
    fun getCacheImage(any: Any?): Bitmap? = null
}