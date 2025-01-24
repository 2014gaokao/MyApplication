package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore

class MediaStoreUtils {

    fun saveImageToMedia(context : Context, bytes : ByteArray, path : String) {
        val values = ContentValues()
        //values.put(MediaStore.Images.Media.DATA, path)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);//在Pictures目录下创建自己的子目录，并向其中写入图片文件
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);//向指定的 ContentProvider 中插入一条新的数据
        val outputStream = context.contentResolver.openOutputStream(uri!!)
        outputStream?.write(bytes)
        outputStream?.close()
    }

}