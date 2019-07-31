package com.pingrex.preview.photoview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Scroller


class AnimPhotoView : PhotoView {

    private var bitmapTransform: BitmapTransform? = null
    private var onEnterAnimEndListener: OnEnterAnimEndListener? = null
    private var onExitAnimEndListener: OnExitAnimEndListener? = null

    private var isPlayingEnterAnim = false

    private var globalOffset: Point? = null
    private var clipBounds: RectF? = null


    constructor(context: Context) : super(context)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context, attr, defStyle)

    /**
     * 获取view的大小
     *
     * @return
     */
    private val viewBounds: Rect
        get() {
            val result = Rect()
            getGlobalVisibleRect(result, globalOffset)
            return result
        }


    override fun initView() {
        super.initView()
        bitmapTransform = BitmapTransform()
        globalOffset = Point()
    }

    override fun draw(canvas: Canvas) {
        // 绘制剪裁后的范围
        if (clipBounds != null) {
            canvas.clipRect(clipBounds!!)
            clipBounds = null
        }
        super.draw(canvas)
    }

    /**
     * 开始进入动画
     * @param from
     * @param container
     * @param l
     */
    fun playEnterAnim(from: Rect?, container: View, l: OnEnterAnimEndListener?) {
        if (from == null) {
            l?.onEnterAnimEnd()
            return
        }
        this.onEnterAnimEndListener = l
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // 开启入场动画
                playEnterAnimInternal(from, container)
                viewTreeObserver.removeOnPreDrawListener(this)
                return false
            }
        })
    }

    /**
     * 开始进入动画
     * @param from
     * @param container
     */
    private fun playEnterAnimInternal(from: Rect?, container: View) {
        if (isPlayingEnterAnim || from == null) return

        val tFrom = Rect(from)
        val to = Rect()
        getGlobalVisibleRect(to, globalOffset)

        tFrom.offset(-globalOffset!!.x, -globalOffset!!.y)
        to.offset(-globalOffset!!.x, -globalOffset!!.y)

        // 计算缩放比率
        val scaleValue = calculateScaleByCenterCrop(to, tFrom)
        // 设置缩放的中心点
        pivotX = tFrom.centerX().toFloat() / to.width()
        pivotY = tFrom.centerY().toFloat() / to.height()

        // 计算平移起始位置的差值
        val diffX = 0f
        val diffY = (to.height() * scaleValue - tFrom.height()) / 2f

        val enterSet = AnimatorSet()

        enterSet.play(ObjectAnimator.ofFloat(this, View.X, tFrom.left - diffX, to.left.toFloat()))
                .with(ObjectAnimator.ofFloat(this, View.Y, tFrom.top - diffY, to.top.toFloat()))
                .with(ObjectAnimator.ofFloat(this, View.SCALE_X, scaleValue, 1f))
                .with(ObjectAnimator.ofFloat(this, View.SCALE_Y, scaleValue, 1f))
                .with(ObjectAnimator.ofFloat(container, View.ALPHA, 0f, 1f))

        enterSet.duration = ANIM_DURATION.toLong()
        enterSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isPlayingEnterAnim = true
            }

            override fun onAnimationEnd(animation: Animator) {
                isPlayingEnterAnim = false
                if (onEnterAnimEndListener != null) {
                    onEnterAnimEndListener!!.onEnterAnimEnd()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                isPlayingEnterAnim = false
            }

            override fun onAnimationRepeat(animation: Animator) {
                isPlayingEnterAnim = true
            }
        })

        // 开启属性动画
        enterSet.start()
    }


    /**
     * 开始退出动画
     * @param to
     * @param alphaView
     * @param listener
     */
    fun playExitAnim(to: Rect?, alphaView: View?, listener: OnExitAnimEndListener?) {
        this.onExitAnimEndListener = listener
        if (to == null) {
            listener?.onExitAnimEnd()
            return
        }
        playExitAnimInternal(to, alphaView)
    }

    /**
     * 开始退出动画
     * @param to
     * @param alphaView
     */
    private fun playExitAnimInternal(to: Rect?, alphaView: View?) {
        if (isPlayingEnterAnim || to == null || mAttacher == null) {
            if (onExitAnimEndListener != null) {
                onExitAnimEndListener!!.onExitAnimEnd()
            }
            return
        }

        if (scale > 1.0f) scale = 1.0f

        val from = viewBounds
        val drawableBounds = getDrawableBounds(drawable)
        val target = Rect(to)
        from.offset(-globalOffset!!.x, -globalOffset!!.y)
        target.offset(-globalOffset!!.x, -globalOffset!!.y)
        if (drawableBounds == null) {
            if (onExitAnimEndListener != null) {
                onExitAnimEndListener!!.onExitAnimEnd()
            }
            return
        }

        // bitmap位移，从当前ImageView平移到缩略图的ImageView
        bitmapTransform!!.animTranslate(from.centerX(), target.centerX(), from.centerY(), target.centerY())

        // 计算缩放值
        val scaleValue = calculateScaleByCenterCrop(drawableBounds, target)

        // 等比缩放
        bitmapTransform!!.animScale(scale, scaleValue, from.centerX(), from.centerY())

        // 设置透明度动画
        if (alphaView != null) {
            bitmapTransform!!.animAlpha(alphaView, 1.0f, 0f)
        }

        // 设置切割动画
        if (target.width() < from.width() || target.height() < from.height()) {
            bitmapTransform!!.animClip(from, target)
        }

        // 开启动画
        bitmapTransform!!.start(object : OnAllFinishListener {
            override fun onAllFinish() {
                if (onExitAnimEndListener != null) {
                    onExitAnimEndListener!!.onExitAnimEnd()
                }
            }
        })
    }

    /**
     * 计算从 from 到 target 的缩放比率
     * @param from
     * @param to
     * @return
     */
    private fun calculateScaleByCenterCrop(from: Rect, to: Rect): Float {
        val imageHeight = from.height()
        val imageWidth = from.width()
        var result: Float
        if (to.width() >= to.height()) {
            // 按最终展示位置的宽度铺满缩放
            result = to.width() * 1f / (imageWidth * 1f)
            //　查看高度是否足够，不够则按高度缩放
            if (imageHeight * result < to.height()) {
                result = to.height() * 1f / (imageHeight * 1f)
            }
        } else {
            // 按最终展示位置的高度铺满缩放
            result = to.height() * 1f / (imageHeight * 1f)
            //　查看宽度是否足够，不够则按宽度缩放
            if (imageWidth * result < to.width()) {
                result = to.width() * 1f / (imageWidth * 1f)
            }
        }
        return result
    }


    /**
     * 根据Drawable获取Rect对象
     * @param d
     * @return
     */
    private fun getDrawableBounds(d: Drawable?): Rect? {
        if (d == null) return null
        val result = Rect()
        val tDrawableRect = d.bounds
        val drawableMatrix = imageMatrix

        val values = FloatArray(9)
        drawableMatrix.getValues(values)

        result.left = values[Matrix.MTRANS_X].toInt()
        result.top = values[Matrix.MTRANS_Y].toInt()
        result.right = (result.left + tDrawableRect.width() * values[Matrix.MSCALE_X]).toInt()
        result.bottom = (result.top + tDrawableRect.height() * values[Matrix.MSCALE_Y]).toInt()

        return result
    }

    interface OnEnterAnimEndListener {
        fun onEnterAnimEnd()
    }

    interface OnExitAnimEndListener {
        fun onExitAnimEnd()
    }

    internal interface OnAllFinishListener {
        fun onAllFinish()
    }

    private inner class BitmapTransform internal constructor() : Runnable {

        internal var targetView: View? = null

        @Volatile
        internal var isRunning: Boolean = false

        internal var translateScroller: Scroller? = null
        internal var scaleScroller: Scroller? = null
        internal var alphaScroller: Scroller? = null
        internal var clipScroller: Scroller? = null

        internal var defaultInterpolator: Interpolator = DecelerateInterpolator()


        internal var scaleCenterX: Int = 0
        internal var scaleCenterY: Int = 0

        internal var scaleX: Float = 0.toFloat()
        internal var scaleY: Float = 0.toFloat()

        internal var alpha: Float = 0.toFloat()

        internal var dx: Int = 0
        internal var dy: Int = 0

        internal var preTranslateX: Int = 0
        internal var preTranslateY: Int = 0

        internal var mClipRect: RectF
        internal var clipTo: RectF
        internal var clipFrom: RectF
        internal var tempMatrix: Matrix

        internal var onAllFinishListener: OnAllFinishListener? = null


        init {
            isRunning = false
            mClipRect = RectF()
            clipTo = RectF()
            clipFrom = RectF()
            tempMatrix = Matrix()
        }

        internal fun animScale(fromX: Float, toX: Float, fromY: Float, toY: Float, centerX: Int, centerY: Int) {
            scaleScroller = Scroller(context, defaultInterpolator)
            this.scaleCenterX = centerX
            this.scaleCenterY = centerY
            scaleScroller!!.startScroll((fromX * PRECISION).toInt(), (fromY * PRECISION).toInt(), ((toX - fromX) * PRECISION).toInt(), ((toY - fromY) * PRECISION).toInt(), ANIM_DURATION)
        }

        internal fun animScale(from: Float, to: Float, centerX: Int, centerY: Int) {
            animScale(from, to, from, to, centerX, centerY)
        }

        internal fun animTranslate(fromX: Int, toX: Int, fromY: Int, toY: Int) {
            translateScroller = Scroller(context, defaultInterpolator)
            preTranslateX = 0
            preTranslateY = 0
            translateScroller!!.startScroll(0, 0, toX - fromX, toY - fromY, ANIM_DURATION)
        }

        internal fun animAlpha(target: View, fromAlpha: Float, toAlpha: Float) {
            alphaScroller = Scroller(context, defaultInterpolator)
            this.targetView = target
            alphaScroller!!.startScroll((fromAlpha * PRECISION).toInt(), 0, ((toAlpha - fromAlpha) * PRECISION).toInt(), 0, ANIM_DURATION)
        }

        internal fun animClip(clipFrom: Rect, clipTo: Rect) {
            clipScroller = Scroller(context, defaultInterpolator)
            this.clipFrom = RectF(clipFrom)
            this.clipTo = RectF(clipTo)

            if (!clipFrom.isEmpty && !clipTo.isEmpty) {
                //算出裁剪比率
                var dx = Math.min(1.0f, clipTo.width() * 1.0f / clipFrom.width() * 1.0f)
                var dy = Math.min(1.0f, clipTo.height() * 1.0f / clipFrom.height() * 1.0f)
                //因为scroller是对起始值和终点值之间的数值影响，所以减去1，如果为0，意味着不裁剪，因为目标值比开始值大，而画布无法再扩大了，所以忽略
                dx -= 1
                dy -= 1
                //从1开始,乘以1w保证精度
                clipScroller!!.startScroll((0 * PRECISION).toInt(), (0 * PRECISION).toInt(), (dx * PRECISION).toInt(), (dy * PRECISION).toInt(), ANIM_DURATION)
            }
        }

        override fun run() {
            var isAllFinish = true

            // 平移图片
            if (translateScroller != null && translateScroller!!.computeScrollOffset()) {
                val curX = translateScroller!!.currX
                val curY = translateScroller!!.currY

                dx += curX - preTranslateX
                dy += curY - preTranslateY

                preTranslateX = curX
                preTranslateY = curY
                isAllFinish = false
            }

            // 缩放图片
            if (scaleScroller != null && scaleScroller!!.computeScrollOffset()) {
                scaleX = scaleScroller!!.currX.toFloat() / PRECISION
                scaleY = scaleScroller!!.currY.toFloat() / PRECISION
                isAllFinish = false
            }

            // 透明图片
            if (alphaScroller != null && alphaScroller!!.computeScrollOffset()) {
                alpha = alphaScroller!!.currX.toFloat() / PRECISION
                isAllFinish = false
            }

            // 切割图片
            if (clipScroller != null && clipScroller!!.computeScrollOffset()) {
                // 获取当前值 切割比率 正值
                val curX = Math.abs(clipScroller!!.currX.toFloat() / PRECISION)
                val curY = Math.abs(clipScroller!!.currY.toFloat() / PRECISION)

                //算出当前的移动像素的综合
                val dx = clipFrom.width() * curX
                val dy = clipFrom.height() * curY
                /*
                 裁剪动画算法是一个。。。初中的简单方程题

                 设裁剪过程中，左边缘移动dl个像素，右边缘移动dr个像素
                 由上面dx算出dl+dr=dx;
                 因为dl和dr的比率可知，因此可以知道dLeft和dRight的相对速率
                 比如左边裁剪1像素，而右边宽度是左边的3倍，则右边应该裁剪3像素才追得上左边
                 联立方程：
                 1：dl+dr=dx;
                 2：dl/dr=a;
                 则由2可得
                 dl=a*dr;
                 代入1
                 dr*(a+1)=dx;
                 可以算得出dr
                 再次代入1可知
                 dl=dx-dr
                 */

                val ratiofLeftAndRight = (clipTo.left - clipFrom.left) / (clipFrom.right - clipTo.right)
                val ratiofTopAndBottom = (clipTo.top - clipFrom.top) / (clipFrom.bottom - clipTo.bottom)

                val dClipRight = dx / (ratiofLeftAndRight + 1)
                val dClipLeft = dx - dClipRight
                val dClipBottom = dy / (ratiofTopAndBottom + 1)
                val dClipTop = dy - dClipBottom


                mClipRect.left = clipFrom.left + dClipLeft
                mClipRect.right = clipFrom.right - dClipRight

                mClipRect.top = clipFrom.top + dClipTop
                mClipRect.bottom = clipFrom.bottom - dClipBottom

                if (!mClipRect.isEmpty) {
                    clipBounds = mClipRect
                }
                isAllFinish = false
            }

            // 判断是否重复
            if (!isAllFinish) {
                setMatrixValue()
                postExecuteSelf()
            } else {
                isRunning = false
                reset()
                if (onAllFinishListener != null) {
                    onAllFinishListener!!.onAllFinish()
                }
            }
        }

        private fun setMatrixValue() {
            if (mAttacher == null) return
            resetSuppMatrix()
            //　是否缩放
            if (scaleScroller != null)
                postMatrixScale(scaleX, scaleY, scaleCenterX.toFloat(), scaleCenterY.toFloat())
            //　是否平移
            if (translateScroller != null)
                postMatrixTranslate(dx.toFloat(), dy.toFloat())
            //　是否改变透明度
            if (alphaScroller != null && targetView != null) targetView!!.alpha = alpha
            applyMatrix()
        }

        private fun postExecuteSelf() {
            if (isRunning) post(this)
        }

        private fun reset() {
            translateScroller = null
            scaleScroller = null
            alphaScroller = null
            clipScroller = null

            scaleCenterX = 0
            scaleCenterY = 0

            scaleX = 0f
            scaleY = 0f

            dx = 0
            dy = 0

            preTranslateX = 0
            preTranslateY = 0

            alpha = 0f
        }

        internal fun stop(reset: Boolean) {
            removeCallbacks(this)
            if (scaleScroller != null)
                scaleScroller!!.abortAnimation()
            if (translateScroller != null)
                translateScroller!!.abortAnimation()
            if (alphaScroller != null)
                alphaScroller!!.abortAnimation()
            if (clipScroller != null)
                clipScroller!!.abortAnimation()

            isRunning = false
            onAllFinishListener = null
            if (reset) reset()
        }

        internal fun start(onAllFinishListener: OnAllFinishListener?) {
            if (isRunning) stop(false)
            this.onAllFinishListener = onAllFinishListener
            isRunning = true
            postExecuteSelf()
        }

    }

    override fun onDetachedFromWindow() {
        bitmapTransform!!.stop(true)
        super.onDetachedFromWindow()
    }


    companion object {
        const val ANIM_DURATION = 300
        const val PRECISION = 10000f
    }
}