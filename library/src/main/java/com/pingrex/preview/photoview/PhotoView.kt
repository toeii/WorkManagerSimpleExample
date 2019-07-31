/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pingrex.preview.photoview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView


open class PhotoView : AppCompatImageView, IPhotoView {

    /**
     * 此项为另外添加，为了让viewpager的destroyItem方法不会让photoview的缩放功能失效(
     * 默认是cleanup在DetachedFromWindow的时候
     * ，viewpager是需要即使DetachedFromWindow也不cleanup)，
     * 如果是除了viewpager以外的地方使用photoview，请勿设置此项目,
     */
    var isCleanOnDetachedFromWindow = true


    protected var mAttacher: PhotoViewAttacher? = null

    private var mPendingScaleType: ImageView.ScaleType? = null

    override val displayRect: RectF?
        get() = mAttacher!!.displayRect

    override var minimumScale: Float
        get() = mAttacher!!.minimumScale
        set(minimumScale) {
            mAttacher!!.minimumScale = minimumScale
        }

    override var mediumScale: Float
        get() = mAttacher!!.mediumScale
        set(mediumScale) {
            mAttacher!!.mediumScale = mediumScale
        }

    override var maximumScale: Float
        get() = mAttacher!!.maximumScale
        set(maximumScale) {
            mAttacher!!.maximumScale = maximumScale
        }

    override var scale: Float
        get() = mAttacher!!.scale
        set(scale) {
            mAttacher!!.scale = scale
        }

    override val visibleRectangleBitmap: Bitmap?
        get() = mAttacher!!.visibleRectangleBitmap

    override val iPhotoViewImplementation: IPhotoView
        get() = mAttacher!!


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }


    protected open fun initView() {
        super.setScaleType(ImageView.ScaleType.MATRIX)
        if (null == mAttacher || null == mAttacher!!.imageView) {
            mAttacher = PhotoViewAttacher(this)
        }

        if (null != mPendingScaleType) {
            scaleType = mPendingScaleType!!
            mPendingScaleType = null
        }
    }


    override fun setRotationTo(rotationDegree: Float) {
        mAttacher!!.setRotationTo(rotationDegree)
    }

    override fun setRotationBy(rotationDegree: Float) {
        mAttacher!!.setRotationBy(rotationDegree)
    }

    override fun canZoom(): Boolean {
        return mAttacher!!.canZoom()
    }

    fun getDisplayRect(matrix: Matrix): RectF? {
        return mAttacher!!.getDisplayRect(matrix)
    }


    override fun getScaleType(): ScaleType {
        return if (null != mAttacher) {
            mAttacher!!.getScaleType()
        } else {
            mPendingScaleType!!
        }
    }

    override fun setScaleType(scaleType: ImageView.ScaleType) {
        if (null != mAttacher) {
            mAttacher!!.setScaleType(scaleType)
        } else {
            mPendingScaleType = scaleType
        }
    }

    override fun getDisplayMatrix(matrix: Matrix) {
        mAttacher!!.getDisplayMatrix(matrix)
    }

    override fun setDisplayMatrix(finalRectangle: Matrix): Boolean {
        return mAttacher!!.setDisplayMatrix(finalRectangle)
    }


    override fun getImageMatrix(): Matrix {
        return mAttacher!!.imageMatrix
    }

    override fun setAllowParentInterceptOnEdge(allow: Boolean) {
        mAttacher!!.setAllowParentInterceptOnEdge(allow)
    }

    override fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        mAttacher!!.setScaleLevels(minimumScale, mediumScale, maximumScale)
    }

    override// setImageBitmap calls through to this method
    fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (null != mAttacher) {
            mAttacher!!.update()
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (null != mAttacher) {
            mAttacher!!.update()
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        if (null != mAttacher) {
            mAttacher!!.update()
        }
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (null != mAttacher) {
            mAttacher!!.update()
        }
        return changed
    }

    override fun setOnMatrixChangeListener(listener: PhotoViewAttacher.OnMatrixChangedListener) {
        mAttacher!!.setOnMatrixChangeListener(listener)
    }

    override fun setOnPhotoTapListener(listener: PhotoViewAttacher.OnPhotoTapListener) {
        mAttacher!!.setOnPhotoTapListener(listener)
    }

    override fun setOnViewTapListener(listener: PhotoViewAttacher.OnViewTapListener) {
        mAttacher!!.setOnViewTapListener(listener)
    }

    override fun setScale(scale: Float, animate: Boolean) {
        mAttacher!!.setScale(scale, animate)
    }

    override fun setScale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        mAttacher!!.setScale(scale, focalX, focalY, animate)
    }


    override fun setZoomable(zoomable: Boolean) {
        mAttacher!!.setZoomable(zoomable)
    }

    override fun setZoomTransitionDuration(milliseconds: Int) {
        mAttacher!!.setZoomTransitionDuration(milliseconds)
    }

    override fun setOnDoubleTapListener(newOnDoubleTapListener: GestureDetector.OnDoubleTapListener) {
        mAttacher!!.setOnDoubleTapListener(newOnDoubleTapListener)
    }

    override fun setOnScaleChangeListener(onScaleChangeListener: PhotoViewAttacher.OnScaleChangeListener) {
        mAttacher!!.setOnScaleChangeListener(onScaleChangeListener)
    }

    override fun setOnSingleFlingListener(onSingleFlingListener: PhotoViewAttacher.OnSingleFlingListener) {
        mAttacher!!.setOnSingleFlingListener(onSingleFlingListener)
    }

    fun setMatrixScale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) {
        mAttacher!!.setMatrixScale(scaleX, scaleY, centerX, centerY)
    }

    fun setMatrixTranslate(dx: Float, dy: Float) {
        mAttacher!!.setMatrixTranslate(dx, dy)
    }

    fun postMatrixScale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) {
        mAttacher!!.postMatrixScale(scaleX, scaleY, centerX, centerY)
    }

    fun postMatrixScale(scaleX: Float, scaleY: Float) {
        mAttacher!!.postMatrixScale(scaleX, scaleY)
    }

    fun postMatrixTranslate(dx: Float, dy: Float) {
        mAttacher!!.postMatrixTranslate(dx, dy)
    }


    fun preMatrixScale(scaleX: Float, scaleY: Float, centerX: Float, centerY: Float) {
        mAttacher!!.preMatrixScale(scaleX, scaleY, centerX, centerY)
    }


    fun preMatrixTranslate(dx: Float, dy: Float) {
        mAttacher!!.preMatrixTranslate(dx, dy)
    }

    fun resetSuppMatrix() {
        mAttacher!!.resetSuppMatrix()
    }

    fun applyMatrix() {
        mAttacher!!.applyMatrix()
    }

    override fun onDetachedFromWindow() {
        if (isCleanOnDetachedFromWindow && mAttacher != null) {
            mAttacher!!.cleanup()
            mAttacher = null
        }
        super.onDetachedFromWindow()
    }

    fun destroy() {
        mAttacher!!.cleanup()
        mAttacher = null
    }
}
