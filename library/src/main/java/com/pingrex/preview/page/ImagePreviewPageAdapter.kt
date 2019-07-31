package com.pingrex.preview.page

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pingerx.preview.R
import com.pingrex.preview.ImagePreview
import com.pingrex.preview.listenner.ImagePreviewLoadListener
import com.pingrex.preview.photoview.AnimPhotoView
import com.pingrex.preview.photoview.PhotoViewAttacher
import com.pingrex.preview.wrapper.ImageEntity


class ImagePreviewPageAdapter(private val context: Context, private val mImageEntities: List<ImageEntity>) : PagerAdapter(), PhotoViewAttacher.OnViewTapListener {

    private var currentView: View? = null

    val primaryImageView: AnimPhotoView?
        get() = if (currentView != null)
            currentView!!.findViewById(R.id.photoView)
        else
            null
    val progressBar: ContentLoadingProgressBar?
        get() = if (currentView != null)
            currentView!!.findViewById(R.id.progressBar)
        else
            null

    private var mListener: OnPageClickListener? = null

    override fun getCount(): Int {
        return mImageEntities.size
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view == any
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, any: Any) {
        super.setPrimaryItem(container, position, any)
        currentView = any as View
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_photoview, container, false)
        val progressBar = view.findViewById<ContentLoadingProgressBar>(R.id.progressBar)
        val photoView = view.findViewById<AnimPhotoView>(R.id.photoView)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)

        // 设置progress的主题色
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ImagePreview.primaryColor != 0) {
            progressBar.indeterminateTintList = ColorStateList.valueOf(ImagePreview.primaryColor)
        }*/

        // 获取对应位置的实体
        val info = this.mImageEntities[position]
        photoView.setOnViewTapListener(this)

        val url: Any? = if (!TextUtils.isEmpty(info.imageUrl)) info.imageUrl else info.thumbnailUrl
        showExcessPic(info, photoView)
        ImagePreview.imageLoader?.onLoadPreviewImage(url, photoView, object : ImagePreviewLoadListener {
            override fun onLoadSuccess(bitmap: Bitmap?) {
                progressBar.visibility = View.GONE
                tvMessage?.visibility = View.GONE
            }

            override fun onLoadFail(msg: String?) {
                progressBar.visibility = View.GONE
                tvMessage?.visibility = View.VISIBLE
                tvMessage?.text = msg ?: "图片加载失败"
            }
        })

        photoView.setOnLongClickListener {
            onLongClick(info)
            return@setOnLongClickListener true
        }

        container.addView(view)
        return view
    }

    /**
     * 长按，展示对话框，可以保存到本地，分享
     */
    private fun onLongClick(info: ImageEntity) {
        val dialog =  BottomSheetDialog(context)
        dialog.setContentView(R.layout.dialog_bottom_preview)

        if (context is Activity) {
            if (context.isFinishing || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && context.isDestroyed)) {
                return
            }
        }
        dialog.show()
        val recyclerView =  dialog.findViewById<RecyclerView>(R.id.dialogRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        val data = arrayListOf<String>()
        data.add(context.getString(R.string.image_dialog_share))
        data.add(context.getString(R.string.image_dialog_save))

        val adapter = DialogItemAdapter(data)
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                dialog.dismiss()
                if (position == 0) {
                    ImagePreview.imagePageListener?.onImageShare(info.imageUrl)
                }else{
                    // 检查权限
                    if (!checkPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) && context is ImagePreviewPageActivity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), ImagePreviewPageActivity.SAVE_PERMISSION_CODE)
                        return
                    }
                    ImagePreview.imagePageListener?.onImageSave(info.imageUrl)
                }
            }
        })
        recyclerView?.adapter = adapter
    }

    fun checkPermission(context: Context?, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            context?.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else {
            context?.packageManager?.checkPermission(permission, context.packageName) == PackageManager.PERMISSION_GRANTED
        }
    }

    inner class DialogItemAdapter(private val data:List<String>) : RecyclerView.Adapter<DialogItemAdapter.ViewHolder>() {

        private var mListener: OnItemClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)=ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.dialog_bottom_item,parent,false))

        override fun getItemCount(): Int  = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvItem.text = data[position]
            holder.itemView.setOnClickListener {
                mListener?.onItemClick(position)
            }
        }

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.mListener = listener
        }
        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val tvItem:TextView = itemView.findViewById(R.id.tvItem)
        }
    }


    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    /**
     * 展示过度图片
     */
    private fun showExcessPic(imageInfo: ImageEntity, imageView: AnimPhotoView) {
        // 先获取大图的缓存图片
        var cacheImage: Bitmap? = ImagePreview.imageLoader?.getCacheImage(imageInfo.imageUrl)
        // 如果大图的缓存不存在,在获取小图的缓存
        if (cacheImage == null)
            cacheImage = ImagePreview.imageLoader?.getCacheImage(imageInfo.thumbnailUrl)
        // 如果没有任何缓存,使用默认图片,否者使用缓存
        if (cacheImage != null) {
            imageView.setImageBitmap(cacheImage)
        }else{
            cacheImage = BitmapFactory.decodeFile(imageInfo.imageUrl.toString())
            if(null != cacheImage){
                imageView.setImageBitmap(cacheImage)
            }
        }

    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun onViewTap(view: View, x: Float, y: Float) {
        mListener?.onPageClick()
    }

    interface OnPageClickListener {
        fun onPageClick()
    }

    fun setOnPageClickListener(listener: OnPageClickListener) {
        this.mListener = listener
    }
}