package com.example.customersupport

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

class GalleryHelper(private val context: Context) {

    fun loadImages(selectedAlbum: String?): List<String> {
        val images = mutableListOf<String>()
        val selection = if (selectedAlbum.isNullOrEmpty()) null else "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = if (selectedAlbum.isNullOrEmpty()) null else arrayOf(selectedAlbum)
        val order = "${MediaStore.Images.Media.DATE_TAKEN} ASC"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATA),
            selection,
            selectionArgs,
            order
        )
        cursor?.use {
            while (it.moveToNext()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                images.add(it.getString(columnIndex))
            }
        }
        return images.reversed()
    }

    fun fetchAlbums(): List<Album> {
        val contentResolver = context.contentResolver
        val albums = mutableListOf<Album>()
        val encounteredAlbums = mutableSetOf<String>() // Set to keep track of encountered albums
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val imagePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val albumName = cursor.getString(bucketNameColumn)
                if (encounteredAlbums.contains(albumName)) {
                    continue
                }
                val imagePath = cursor.getString(imagePathColumn)
                val albumImageCount = cursor.countImagesInAlbum(albumName, contentResolver)
                encounteredAlbums.add(albumName)
                albums.add(Album(listOf(imagePath), albumName, albumImageCount))
            }
        }
        return albums
    }

    private fun Cursor.countImagesInAlbum(albumName: String, contentResolver: ContentResolver): Int {
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(albumName)
        return contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            cursor.count
        } ?: 0
    }
}
