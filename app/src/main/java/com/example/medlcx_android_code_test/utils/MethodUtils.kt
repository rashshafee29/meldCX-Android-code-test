package com.example.medlcx_android_code_test.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log

object MethodUtils {
    fun getImageFromMediaStore(appContext: Context, findFileByName : String): Uri {

        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME)

        val cUri = Uri.parse("")

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appContext.contentResolver.query(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                imageProjection,
                null,
                null,
                null
            )
        } else {
            appContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                null
            )
        }

        cursor.use {
            it?.let {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                            id)
                    } else {
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id)
                    }
                    Log.e("RashidShafee","name: " + name)
                    Log.e("RashidShafee","findFileByName: " + findFileByName)
                    if(findFileByName == name) {
                        Log.e("RashidShafee","Images: $id $name $contentUri")
                        return contentUri
                    }
                }
            }?: kotlin.run {
                Log.e("TAG", "Cursor is null!") }
        }
        return cUri
    }
}