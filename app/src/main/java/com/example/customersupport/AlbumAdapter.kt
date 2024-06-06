package com.example.customersupport

import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load

class AlbumAdapter(
    private val listener: OnAlbumClickListener
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    private var albumsList = listOf<Album>()

    interface OnAlbumClickListener {
        fun onAlbumClick(album: Album)
    }

    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val albumName: TextView = itemView.findViewById(R.id.album_name)
        val albumCount: TextView = itemView.findViewById(R.id.album_count)
        val albumFirstPic: ImageView = itemView.findViewById(R.id.gallery_album_first_pic)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val album = albumsList[position]
                listener.onAlbumClick(album)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.all_album_layout, parent, false)
        return AlbumViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val currentAlbum = albumsList[position]
        holder.albumName.text = currentAlbum.albumName
        holder.albumCount.text = currentAlbum.albumCount.toString()
        val mostRecentImage =
            fetchMostRecentImageForAlbum(holder.itemView.context, currentAlbum.albumName)
        if (mostRecentImage != null) {
            holder.albumFirstPic.load(mostRecentImage)
        } else {
            Toast.makeText(holder.itemView.context, "No Images", Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int {
        return albumsList.size
    }

    fun submitList(newList: List<Album>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = albumsList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                albumsList[oldItemPosition] == newList[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                albumsList[oldItemPosition] == newList[newItemPosition]
        })
        albumsList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    private fun fetchMostRecentImageForAlbum(context: Context, albumName: String): String? {
        val projection = arrayOf(
            MediaStore.Images.Media.DATA
        )
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(albumName)
        return context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            } else {
                null
            }
        }
    }
}