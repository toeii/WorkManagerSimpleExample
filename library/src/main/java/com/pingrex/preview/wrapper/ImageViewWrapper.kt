package com.pingrex.preview.wrapper

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat


class ImageViewWrapper constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val textPaint: TextPaint              //文字的画笔
    private var msg = ""                          //要绘制的文字
    private var isGif: Boolean = false
    private val mGifBitmap: Bitmap? = null
    private val mGifPlaceHolderBitmap: Bitmap? = null

    //显示更多的数量
    var moreNum = 0
        set(moreNum) {
            field = moreNum
            msg = moreNum.toString() + "图"
            invalidate()
        }

    //默认的遮盖颜色
    var maskColor = -0x78000000
        set(maskColor) {
            field = maskColor
            invalidate()
        }

    //显示文字的大小单位sp
    private var textSize = 12f
    //显示文字的颜色
    var textColor = -0x1
        set(textColor) {
            field = textColor
            textPaint.color = textColor
            invalidate()
        }


    init {

        //转化单位
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getContext().resources.displayMetrics)

        textPaint = TextPaint()
        textPaint.textAlign = Paint.Align.CENTER  //文字居中对齐
        textPaint.isAntiAlias = true                //抗锯齿
        textPaint.textSize = textSize             //设置文字大小
        textPaint.color = this.textColor               //设置文字颜色


        // TODO GIF图片的占位图
        //mGifBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_image_gif);
        //mGifPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_image_gif_placeholder);
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (this.moreNum > 0) {
            canvas.drawColor(this.maskColor)
            // 绘制的文字位置
            //            float baseY = getHeight() / 2 - (textPaint.ascent() + textPaint.descent()) / 2;
            //            float baseX = 0;

            // TODO 绘制弧形背景
            val baseY = 50f   // 上边距
            val baseX = width.toFloat() - (textPaint.ascent() + textPaint.descent()) - 100f   // 左右边距
            canvas.drawText(msg, baseX, baseY, textPaint)
        }

        if (isGif) {
            // 是gif图片
            if (mGifBitmap != null) {
                canvas.drawBitmap(mGifBitmap, (width - mGifBitmap.width).toFloat(), (height - mGifBitmap.height).toFloat(), null)
            }
        } else {
            // 不是gif图片就绘制透明图片上去
            if (mGifPlaceHolderBitmap != null) {
                canvas.drawBitmap(mGifPlaceHolderBitmap, (width - mGifPlaceHolderBitmap.width).toFloat(), (height - mGifPlaceHolderBitmap.height).toFloat(), null)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val drawable = drawable
                if (drawable != null) {
                    /**
                     * 默认情况下，所有的从同一资源（R.drawable.XXX）加载来的drawable实例都共享一个共用的状态，
                     * 如果你更改一个实例的状态，其他所有的实例都会收到相同的通知。
                     * 使用使 mutate 可以让这个drawable变得状态不定。这个操作不能还原（变为不定后就不能变为原来的状态）。
                     * 一个状态不定的drawable可以保证它不与其他任何一个drawabe共享它的状态。
                     * 此处应该是要使用的 mutate()，但是在部分手机上会出现点击后变白的现象，所以没有使用
                     * 目前这种解决方案没有问题
                     */
                    drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                val drawableUp = drawable
                if (drawableUp != null) {
                    drawableUp.clearColorFilter()
                    ViewCompat.postInvalidateOnAnimation(this)
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setImageDrawable(null)
    }

    fun getTextSize(): Float {
        return textSize
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        textPaint.textSize = textSize
        invalidate()
    }

    fun setIsGif(isGif: Boolean) {
        this.isGif = isGif
        if (isGif) invalidate()
    }
}