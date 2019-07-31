package com.pingrex.preview.listenner

/**
 * @author Pinger
 * @since 2018/12/23 11:18
 */
interface ImagePageListener {


    /**
     * 图片浏览页面，图片保存到本地
     */
    fun onImageSave(url: String?)

    /**
     * 图片浏览页面，图片分享
     */
    fun onImageShare(url:String?)
}