package com.pingrex.preview.wrapper

import android.os.Parcel
import android.os.Parcelable


/**
 * 图片的实体类，展示图片时只需要将数据包装到该类
 */
data class ImageEntity(
        var imageUrl: String? = null,          // 原图默认等于缩略图
        var thumbnailUrl: String? = imageUrl,  // 缩略图
        var imageHeight: Int = 0,              // 图片高度
        var imageWidth: Int = 0,               // 图片宽度
        var imageViewX: Int = 0,               // 控件x轴位置
        var imageViewY: Int = 0                // 控件y轴位置
) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(imageUrl)
                parcel.writeString(thumbnailUrl)
                parcel.writeInt(imageHeight)
                parcel.writeInt(imageWidth)
                parcel.writeInt(imageViewX)
                parcel.writeInt(imageViewY)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<ImageEntity> {
                override fun createFromParcel(parcel: Parcel): ImageEntity {
                        return ImageEntity(parcel)
                }

                override fun newArray(size: Int): Array<ImageEntity?> {
                        return arrayOfNulls(size)
                }
        }


}