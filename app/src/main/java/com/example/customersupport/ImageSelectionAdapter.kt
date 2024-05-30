package com.example.customersupport

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class ImageSelectionAdapter(
    private val viewModel: SupportViewModel,
    private val images: MutableList<ImageSelectionDataClass>,
    private val itemClickListener: ImageSelectionAdapter.OnItemClickListener,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_ADD_MORE = 2
        private const val MAX_IMAGES = 5
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_IMAGE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.multiple_image_layout, parent, false)
            ImageViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.add_more_layout, parent, false)
            AddMoreViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            holder.bind(images[position])
        }
        if (holder is AddMoreViewHolder) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return if (images.size < MAX_IMAGES) images.size + 1 else images.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < images.size) VIEW_TYPE_IMAGE else VIEW_TYPE_ADD_MORE
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ShapeableImageView = itemView.findViewById(R.id.image_view)
        private val cancelImageView: ShapeableImageView =
            itemView.findViewById(R.id.image_view_cancel)
        private val imageName:TextView=itemView.findViewById(R.id.tv_image_name)

        fun bind(imageItem: ImageSelectionDataClass) {
            val position = adapterPosition
            imageView.transitionName ="picked_image$position"
            Glide.with(itemView.context)
                .load(imageItem.imageResId) // Assuming imageResId is a URL string
                .into(imageView)
            imageName.text = imageItem.imageName
            imageView.setSafeOnClickListener {
                //Toast.makeText(itemView.context, imageItem.imageResId, Toast.LENGTH_LONG).show()
                itemClickListener.sendSingleImage(imageItem.imageResId,imageView, imageItem.imageName)
            }
            cancelImageView.setSafeOnClickListener{
                removeImage(adapterPosition)

            }
        }
    }

    inner class AddMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setSafeOnClickListener{
                itemClickListener.onItemClick()
            }
        }
        fun bind() {
            itemView.visibility =
                if (images.size == 0 || images.size == 5) View.GONE else View.VISIBLE
        }
    }

    private fun removeImage(position: Int) {
        if (position >= 0 && position < images.size) {
            images.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
            if (images.size == 0) {
                itemClickListener.changeUi()
            }
            itemClickListener.updatedImg(images)
            viewModel.removeImage(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateImages(newImages: List<ImageSelectionDataClass>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = images.size
            override fun getNewListSize(): Int = newImages.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return images[oldItemPosition].imageResId == newImages[newItemPosition].imageResId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return images[oldItemPosition] == newImages[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        images.clear()
        images.addAll(newImages)
        diffResult.dispatchUpdatesTo(this)
    }

    interface OnItemClickListener {
        fun onItemClick()
        fun changeUi()
        fun updatedImg(images: MutableList<ImageSelectionDataClass>)
        fun sendSingleImage(imageResId: String,imageView:ImageView, imageName:String)
    }
}

//        val availableSpace = 5 - images.size
//        val imagesToAdd = if (newImages.size <= availableSpace) newImages else newImages.take(availableSpace)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        images.addAll(imagesToAdd)
////        Log.d(TAG, "allImage:${images.size}")
////        Log.d(TAG, "newImage:${newImages.size}")
////        Log.d(TAG, "imageAdding:${imagesToAdd.size}")
//        diffResult.dispatchUpdatesTo(this)