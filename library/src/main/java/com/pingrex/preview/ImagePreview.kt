package com.pingrex.preview

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.pingerx.preview.R
import com.pingrex.preview.listenner.ImagePageListener
import com.pingrex.preview.listenner.ImagePreviewLoader
import com.pingrex.preview.wrapper.ImageEntity
import com.pingrex.preview.wrapper.ImagePreviewAdapter
import com.pingrex.preview.wrapper.ImageViewWrapper
import java.util.*


/**
 * @author Pinger
 * @since 2018/01/23 09:14
 * 自定义九宫格图片展示控件
 */
class ImagePreview : ViewGroup {

    var maxSize = 9                            // 最大显示的图片数
    private var gridSpacing = 3                // 宫格间距，单位dp
    private var displayMode = MODE_GRID        // 默认使用fill模式
    private var isAnimShow = true              // 是否动画打开

    private var columnCount: Int = 0           // 列数
    private var rowCount: Int = 0              // 行数
    private var gridWidth: Int = 0             // 宫格宽度
    private var gridHeight: Int = 0            // 宫格高度
    private var singleImageSize = 250          // 单张图片时的最大大小,单位dp
    private var singleImageRatio = 1.0f        // 单张图片的宽高比(宽/高)

    private var mImageEntities: List<ImageEntity>? = null
    private var mAdapter: ImagePreviewAdapter? = null

    // 第一次attach的时候，不加载图片，等待layout完后才加载。之后每次attach时加载，重新设置adapter的时候这个标记也会重置。
    private var isFirstAttach: Boolean = false

    companion object {
        const val DEFAULT_COLUMN_COUNT = 3   // 默认列数
        const val MODE_FILL = 0          // 填充模式，类似于微信
        const val MODE_GRID = 1          // 网格模式，类似于QQ，4张图会 2X2布局

        var imageLoader: ImagePreviewLoader? = null        // 全局的图片加载器(必须设置,否者不显示图片)
        var imagePageListener: ImagePageListener? = null
        var primaryColor: Int = 0                           // 主题色
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet?) {

        val dm = context.resources.displayMetrics
        gridSpacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gridSpacing.toFloat(), dm).toInt()
        singleImageSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, singleImageSize.toFloat(), dm).toInt()

        val a = context.obtainStyledAttributes(attrs, R.styleable.ImagePreview)
        gridSpacing = a.getDimension(R.styleable.ImagePreview_imageGridSpacing, gridSpacing.toFloat()).toInt()
        singleImageSize = a.getDimensionPixelSize(R.styleable.ImagePreview_imageSingleSize, singleImageSize)
        singleImageRatio = a.getFloat(R.styleable.ImagePreview_imageSingleRatio, singleImageRatio)
        maxSize = a.getInt(R.styleable.ImagePreview_imageMaxSize, maxSize)
        displayMode = a.getInt(R.styleable.ImagePreview_imageDisplayMode, displayMode)
        isAnimShow = a.getBoolean(R.styleable.ImagePreview_imageAnimShow, isAnimShow)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = View.MeasureSpec.getSize(widthMeasureSpec)
        var height = 0
        // 计算能容纳子视图的最大宽度
        val totalWidth = width - paddingLeft - paddingRight
        if (mImageEntities?.isNotEmpty() == true) {
            if (mImageEntities!!.size == 1) {
                // 如果只有一张图片
                gridWidth = if (singleImageSize > totalWidth) totalWidth else singleImageSize
                gridHeight = (gridWidth / singleImageRatio).toInt()
                // 矫正图片显示区域大小，不允许超过最大显示范围
                if (gridHeight > singleImageSize) {
                    val ratio = singleImageSize * 1.0f / gridHeight
                    gridWidth = (gridWidth * ratio).toInt()
                    gridHeight = singleImageSize
                }
            } else {
                // 这里无论是几张图片，宽高都按总宽度的 1/3
                gridHeight = (totalWidth - gridSpacing * 2) / 3
                gridWidth = gridHeight
            }
            width = gridWidth * columnCount + gridSpacing * (columnCount - 1) + paddingLeft + paddingRight
            height = gridHeight * rowCount + gridSpacing * (rowCount - 1) + paddingTop + paddingBottom
        }
        setMeasuredDimension(width, height)
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mImageEntities != null) {
            val childrenCount = mImageEntities!!.size
            for (i in 0 until childrenCount) {
                val childrenView = getChildAt(i) as ImageView
                // 第几行
                val rowNum = i / columnCount
                // 第几列
                val columnNum = i % columnCount
                // 计算上下左右的位置
                val left = (gridWidth + gridSpacing) * columnNum + paddingLeft
                val top = (gridHeight + gridSpacing) * rowNum + paddingTop
                val right = left + gridWidth
                val bottom = top + gridHeight
                childrenView.layout(left, top, right, bottom)
            }
            if (isFirstAttach) {
                isFirstAttach = false
                showPhoto()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isFirstAttach)
            showPhoto()
    }

    /**
     * 设置适配器
     */
    fun setAdapter(adapter: ImagePreviewAdapter) {
        mAdapter = adapter
        isFirstAttach = true
        // 获取图片列表
        var imageEntities = adapter.imageEntities
        // 控制这个控件是否展示
        if (imageEntities.isEmpty()) {
            visibility = View.GONE
            mImageEntities = null
            return
        } else {
            visibility = View.VISIBLE
        }

        var imageCount = imageEntities.size
        // 如果长度大于规定的最大长度则截取
        if (maxSize in 1..(imageCount - 1)) {
            imageEntities = imageEntities.subList(0, maxSize)
            imageCount = imageEntities.size   //再次获取图片数量
        }

        //默认是3列显示，行数根据图片的数量决定
        columnCount = DEFAULT_COLUMN_COUNT
        rowCount = imageCount / columnCount + if (imageCount % columnCount == 0) 0 else 1

        //grid模式下，显示4张使用2X2模式
        if (displayMode == MODE_GRID && imageCount == 4) {
            rowCount = 2
            columnCount = 2

            // 特殊情况，只有一张图片或者两张图片时，写死
        } else if (imageCount == 1) {
            rowCount = 1
            columnCount = 1
        } else if (imageCount == 2) {
            rowCount = 1
            columnCount = 2
        }


        //保证View的复用，避免重复创建
        if (mImageEntities != null) {
            // 如果是重新设置的Adapter
            val oldViewCount = mImageEntities!!.size
            if (oldViewCount > imageCount) {
                // 移除多余的
                removeViews(imageCount, oldViewCount - imageCount)
            } else if (oldViewCount < imageCount) {
                // 添加不足的
                for (i in oldViewCount until imageCount) {
                    val iv = getImageView(i) ?: return
                    addView(iv, generateDefaultLayoutParams())
                }
            }
        } else {
            for (i in 0 until imageCount) {
                // 获取一个设置了点击监听的ImageView
                val iv = getImageView(i) ?: return
                // 添加到父容器中
                addView(iv, generateDefaultLayoutParams())
            }
        }
        mImageEntities = imageEntities
    }

    private fun showPhoto() {
        if (mImageEntities != null) {
            for (i in 0 until childCount) {
                val entity = mImageEntities!![i]
                if (getChildAt(i) is ImageViewWrapper) {
                    val imagePreviewWrapper = getChildAt(i) as ImageViewWrapper
                    // 加载图片优先加载缩略图，没有就加载原图
                    val url = if (!TextUtils.isEmpty(entity.thumbnailUrl)) entity.thumbnailUrl else entity.imageUrl
                    val cacheImage = BitmapFactory.decodeFile(url)
                    if(null != cacheImage){
                        imageLoader?.onLoadPreviewImage(url, imagePreviewWrapper)
                        imagePreviewWrapper.setImageBitmap(cacheImage)
                    }
                }
            }
        }
    }

    /**
     * 获得 ImageView 保证了 ImageView 的重用
     */
    private fun getImageView(position: Int): ImageView? {
        val imageView = mAdapter?.generateImageView(context)
        imageView?.setOnClickListener {
            mAdapter?.onImageItemClick(context, this@ImagePreview, position,
                    mAdapter?.imageEntities, getImageViewsDrawableRects(), maxSize)
        }
        return imageView
    }

    private fun getDrawableBoundsInView(iv: ImageView, itemTopInList: Int): Rect {
        val d = iv.drawable
        val result = Rect()
        iv.getGlobalVisibleRect(result)
        val tDrawableRect = d.bounds
        val drawableMatrix = iv.imageMatrix
        val values = FloatArray(9)
        drawableMatrix?.getValues(values)

        result.left += values[2].toInt()
        result.top += values[5].toInt()
        result.right = (result.left.toFloat() + tDrawableRect.width().toFloat() * if (values[0] == 0.0f) 1.0f else values[0]).toInt()
        result.bottom = (result.top.toFloat() + tDrawableRect.height().toFloat() * if (values[4] == 0.0f) 1.0f else values[4]).toInt()

        if (itemTopInList > iv.top) {
            // 该ImageView没有完整展示在列表中
            // Rect需要向上偏移一定距离
            result.top -= itemTopInList - iv.top
            result.bottom -= itemTopInList - iv.top
        }
        return result
    }


    /**
     * 获取图片的资源矩阵集合
     *　如果缩略图没有完整展示，则需要计算出实际偏移的坐标
     * @return
     */
    private fun getImageViewsDrawableRects(): List<Rect>? {
        if (!isAnimShow) return null
        val childCount = this.childCount
        if (childCount <= 0) {
            return null
        } else {
            var top = 0
            if (parent != null && parent.parent != null) {
                val topContainer = parent.parent as View
                if (topContainer.top < 0) {
                    top = Math.abs(topContainer.top) - (parent as View).top - getTop()
                }
            }
            val viewRects = LinkedList<Rect>()

            for (i in 0 until childCount) {
                val v = this.getChildAt(i) as? ImageView?
                if (v != null && v.drawable != null) {
                    val rect = this.getDrawableBoundsInView(v, top)
                    viewRects.add(rect)
                }
            }

            return viewRects
        }
    }

    /**
     * 设置宫格间距
     */
    fun setGridSpacing(spacing: Int) {
        gridSpacing = spacing
    }

    /**
     * 设置只有一张图片时的宽和高
     */
    fun setSingleImageSize(maxImageSize: Int) {
        singleImageSize = maxImageSize
    }

    /**
     * 设置只有一张图片时的宽高比
     */
    fun setSingleImageRatio(ratio: Float) {
        singleImageRatio = ratio
    }

    /**
     * 是否是否展示过度动画
     */
    fun setAnimShow(isAnimShow: Boolean) {
        this.isAnimShow = isAnimShow
    }

}