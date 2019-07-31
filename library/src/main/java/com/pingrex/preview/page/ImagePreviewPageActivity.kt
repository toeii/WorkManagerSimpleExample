package com.pingrex.preview.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.pingerx.preview.R
import com.pingrex.preview.ImagePreview
import com.pingrex.preview.photoview.AnimPhotoView
import com.pingrex.preview.wrapper.HackyViewPager
import com.pingrex.preview.wrapper.ImageEntity


/**
 * 图片展示详情页
 */
class ImagePreviewPageActivity : AppCompatActivity(), ViewTreeObserver.OnPreDrawListener {

    private val mPhotoContainer by lazy { findViewById<View>(R.id.photo_container) }
    private val mViewPager by lazy { findViewById<HackyViewPager>(R.id.viewPager) }
    private val mTvPager by lazy { findViewById<TextView>(R.id.tv_pager) }
    private val mRootView by lazy { findViewById<RelativeLayout>(R.id.rootView) }

    private lateinit var imagePreviewAdapter: ImagePreviewPageAdapter
    private var mImageEntities: List<ImageEntity> = ArrayList()
    private var mImageRect: List<Rect>? = null
    private var maxImageSize = 9
    private var currentItem = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        setContentView(R.layout.activity_preview)

        initView()
        initEvent()
    }

    private fun initView() {
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)

        mImageEntities = intent.getParcelableArrayListExtra(IMAGE_ENTITY)
        val rects: List<Rect>? = intent.getParcelableArrayListExtra(IMAGE_RECT)
        if (rects != null && rects.isNotEmpty()) {
            mImageRect = rects
        }
        maxImageSize = intent.getIntExtra(IMAGE_SIZE, maxImageSize)
        currentItem = intent.getIntExtra(CURRENT_ITEM, currentItem)

        imagePreviewAdapter = ImagePreviewPageAdapter(this, mImageEntities)
        mViewPager.adapter = imagePreviewAdapter
        mViewPager.currentItem = currentItem
        if (mImageEntities.isEmpty()) return
        mTvPager.text = String.format(getString(R.string.image_count), (currentItem + 1).toString(), mImageEntities.size.toString())
        // 如果只有一页则隐藏页数
        if (mImageEntities.size == 1) mTvPager.visibility = View.GONE
    }


    private fun initEvent() {
        // 监听点击退出预览界面
        imagePreviewAdapter.setOnPageClickListener(object : ImagePreviewPageAdapter.OnPageClickListener {
            override fun onPageClick() {
                finishActivityAnim()
            }
        })
        mViewPager.viewTreeObserver.addOnPreDrawListener(this)
        mViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                currentItem = position
                mTvPager.text = String.format(getString(R.string.image_count), (currentItem + 1).toString(), mImageEntities.size.toString())
            }
        })
    }


    override fun onBackPressed() {
        finishActivityAnim()
    }


    /**
     * **************************** OnPreDrawListener ****************************
     * 准备绘制开始动画
     */
    override fun onPreDraw(): Boolean {
        mRootView.viewTreeObserver.removeOnPreDrawListener(this)
        initVisibility()

        val photoView = imagePreviewAdapter.primaryImageView
        if (photoView == null) {
            initStartVisibility()
            return true
        }
        val startRect = mImageRect?.get(currentItem)
        // 图片的入场动画
        photoView.playEnterAnim(startRect, mPhotoContainer, object : AnimPhotoView.OnEnterAnimEndListener {
            override fun onEnterAnimEnd() {
                initStartVisibility()
            }
        })
        return true
    }

    /**
     * 初始化视图的显示
     */
    private fun initVisibility() {
        mTvPager.visibility = View.GONE
    }

    private fun initStartVisibility() {
        mTvPager.visibility = View.VISIBLE
    }

    /**
     * activity的退场动画
     */
    private fun finishActivityAnim() {
        mTvPager.visibility = View.GONE
        // 加载进度条也隐藏
        imagePreviewAdapter.progressBar?.visibility = View.GONE
        val currentPhotoView = imagePreviewAdapter.primaryImageView
        if (currentPhotoView == null) {
            super.finish()
            return
        }

        val endRect: Rect? = if (currentItem > maxImageSize - 1) {
            mImageRect?.get(maxImageSize - 1)
        } else {
            mImageRect?.get(currentItem)
        }
        currentPhotoView.playExitAnim(endRect, mPhotoContainer, object : AnimPhotoView.OnExitAnimEndListener {
            override fun onExitAnimEnd() {
                super@ImagePreviewPageActivity.finish()
                // 禁用Activity动画
                overridePendingTransition(0, 0)
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == SAVE_PERMISSION_CODE) {
            ImagePreview.imagePageListener?.onImageSave(mImageEntities[currentItem].imageUrl)
        }
    }

    companion object {
        const val SAVE_PERMISSION_CODE = 1201   // 存储文件权限返回码

        private const val IMAGE_ENTITY = "IMAGE_ENTITY"    // 图片实体对象
        private const val CURRENT_ITEM = "CURRENT_ITEM"    // 当前的条目位置
        private const val IMAGE_RECT = "IMAGE_RECT"        // 图片矩阵
        private const val IMAGE_SIZE = "IMAGE_SIZE"        // 图片最大数量

        fun start(context: Context?, imageEntities: List<ImageEntity>, imageRects: List<Rect>? = null, position: Int = 0, maxSize: Int = 9) {
            val rects = arrayListOf<Rect>()
            if (imageRects != null) {
                rects.addAll(imageRects)
            }
            val images = arrayListOf<ImageEntity>()
            images.addAll(imageEntities)
            start(context, images, rects, position, maxSize)
        }

        fun start(context: Context?, imageEntities: ArrayList<ImageEntity>, imageRects: ArrayList<Rect>? = null, position: Int = 0, maxSize: Int = 9) {
            if (context == null) return
            val intent = Intent(context, ImagePreviewPageActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList(IMAGE_ENTITY, imageEntities)
            bundle.putParcelableArrayList(IMAGE_RECT, imageRects)
            bundle.putInt(IMAGE_SIZE, maxSize)
            bundle.putInt(CURRENT_ITEM, position)
            intent.putExtras(bundle)
            // 启动预览图片界面
            context.startActivity(intent)
            // 禁用Activity动画
            (context as? Activity)?.overridePendingTransition(0, 0)
        }

        fun start(context: Context?, entity: ImageEntity, rect: Rect? = null) {
            val imageEntities = arrayListOf<ImageEntity>()
            imageEntities.add(entity)
            val rects = arrayListOf<Rect>()
            if (rect != null) {
                rects.add(rect)
            }
            start(context, imageEntities, rects)
        }
    }
}
