package com.pingrex.preview.wrapper

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.widget.ImageView
import com.pingrex.preview.ImagePreview
import com.pingrex.preview.page.ImagePreviewPageActivity
import java.io.Serializable


/**
 * 图片浏览的适配器
 * 用来生成预览的图片和实现图片的点击
 */
class ImagePreviewAdapter(var imageEntities: List<ImageEntity>) : Serializable {

    /**
     * 上一次的点击时间，避免重复点击
     */
    private var mLastClickTime = 0L

    /**
     * 生成ImageView容器的方式，默认使用ImageViewPreviewWrapper类，即点击图片后，图片会有蒙板效果
     * 如果需要自定义图片展示效果，重写此方法即可
     *
     * @param context 上下文
     * @return 生成的 ImageView
     */
    fun generateImageView(context: Context): ImageView {
        val imageView = ImageViewWrapper(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setBackgroundColor(Color.parseColor("#E6EBF1"))
        return imageView
    }

    /**
     * 如果要实现图片点击的逻辑，重写此方法即可
     *
     * @param context      上下文
     * @param imagePreview 九宫格控件
     * @param index        当前点击图片的的索引
     * @param imageInfo    图片地址的数据集合
     * @param imageRects
     * @param maxImageSize
     */
    fun onImageItemClick(context: Context, imagePreview: ImagePreview, index: Int, imageInfo: List<ImageEntity>?, imageRects: List<Rect>?, maxImageSize: Int) {
        mLastClickTime = System.currentTimeMillis()

        val imageEntities = arrayListOf<ImageEntity>()
        if (imageInfo != null) {
            // 纠正图片位置
            for (i in imageInfo.indices) {
                val imageEntity = imageInfo[i]
                // 获取图片展示的对应ImageView
                val imageView = if (i < imagePreview.maxSize) {
                    imagePreview.getChildAt(i)
                } else {
                    //如果图片的数量大于显示的数量，则超过部分的返回动画统一退回到最后一个图片的位置
                    imagePreview.getChildAt(imagePreview.maxSize - 1)
                }
                imageEntity.imageWidth = imageView.width
                imageEntity.imageHeight = imageView.height
                val points = IntArray(2)
                imageView.getLocationOnScreen(points)
                // 图片位置
                imageEntity.imageViewX = points[0]
                imageEntity.imageViewY = points[1]
                imageEntities.add(imageEntity)
            }
        }

        val rects = arrayListOf<Rect>()
        if (imageRects != null) {
            rects.addAll(imageRects)
        }

        ImagePreviewPageActivity.start(context, imageEntities, rects, index, maxImageSize)
    }

}