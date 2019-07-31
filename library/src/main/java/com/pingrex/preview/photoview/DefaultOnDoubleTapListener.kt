package com.pingrex.preview.photoview

import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Provided default implementation of GestureDetector.OnDoubleTapListener, to be overridden with custom behavior, if needed
 *
 * &nbsp;
 */
class DefaultOnDoubleTapListener
/**
 * Default constructor
 *
 * @param photoViewAttacher PhotoViewAttacher to bind to
 */
(photoViewAttacher: PhotoViewAttacher) : GestureDetector.OnDoubleTapListener {

    private var photoViewAttacher: PhotoViewAttacher? = null

    init {
        setPhotoViewAttacher(photoViewAttacher)
    }

    /**
     * Allows to change PhotoViewAttacher within range of single instance
     *
     * @param newPhotoViewAttacher PhotoViewAttacher to bind to
     */
    fun setPhotoViewAttacher(newPhotoViewAttacher: PhotoViewAttacher) {
        this.photoViewAttacher = newPhotoViewAttacher
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (this.photoViewAttacher == null)
            return false

        val imageView = photoViewAttacher!!.imageView

        if (null != photoViewAttacher!!.getOnPhotoTapListener()) {
            val displayRect = photoViewAttacher!!.displayRect

            if (null != displayRect) {
                val x = e.x
                val y = e.y

                // Check to see if the user tapped on the photo
                if (displayRect.contains(x, y)) {

                    val xResult = (x - displayRect.left) / displayRect.width()
                    val yResult = (y - displayRect.top) / displayRect.height()

                    photoViewAttacher!!.getOnPhotoTapListener()!!.onPhotoTap(imageView!!, xResult, yResult)
                    return true
                } else {
                    photoViewAttacher!!.getOnPhotoTapListener()!!.onOutsidePhotoTap()
                }
            }
        }
        if (null != photoViewAttacher!!.getOnViewTapListener()) {
            photoViewAttacher!!.getOnViewTapListener()!!.onViewTap(imageView!!, e.x, e.y)
        }

        return false
    }

    override fun onDoubleTap(ev: MotionEvent): Boolean {
        if (photoViewAttacher == null)
            return false

        try {
            val scale = photoViewAttacher!!.scale
            val x = ev.x
            val y = ev.y

            if (scale < photoViewAttacher!!.mediumScale) {
                photoViewAttacher!!.setScale(photoViewAttacher!!.mediumScale, x, y, true)
            } else if (scale >= photoViewAttacher!!.mediumScale && scale < photoViewAttacher!!.maximumScale) {
                photoViewAttacher!!.setScale(photoViewAttacher!!.maximumScale, x, y, true)
            } else {
                photoViewAttacher!!.setScale(photoViewAttacher!!.minimumScale, x, y, true)
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            // Can sometimes happen when getX() and getY() is called
        }

        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        // Wait for the confirmed onDoubleTap() instead
        return false
    }

}
