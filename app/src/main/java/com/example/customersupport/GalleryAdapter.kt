package com.example.customersupport


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customersupport.databinding.PhotoLayoutBinding
import java.io.File

class GalleryAdapter(
    private val galleryViewModel: SupportViewModel,
    private var isGalleryGranted: Boolean,
    private val itemClickListener: OnItemClickListener,
) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    private val VIEW_TYPE_GALLERY = 0
    private val VIEW_TYPE_IMAGE = 1
    private var isAlbumSelected: Boolean = false
    private var imagesList = listOf<String>()
    private val selectedImages = mutableListOf<String>()
    fun updateAlbumSelection(selectedAlbum: String?) {
        isAlbumSelected = !selectedAlbum.isNullOrEmpty()
        notifyItemRangeChanged(0, itemCount)
    }
    fun updateData(newImagesList: List<String>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = imagesList.size
            override fun getNewListSize(): Int = newImagesList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                imagesList[oldItemPosition] == newImagesList[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                imagesList[oldItemPosition] == newImagesList[newItemPosition]
        })
        imagesList = newImagesList
        diffResult.dispatchUpdatesTo(this)
        galleryViewModel.updateImages(newImagesList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryAdapter.ViewHolder {
        return if (viewType == VIEW_TYPE_GALLERY) {
            val binding =
                PhotoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding.root, viewType)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_open_gallery, parent, false)
            ViewHolder(view, viewType)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GalleryAdapter.ViewHolder, position: Int) {
        if (holder.viewType == VIEW_TYPE_GALLERY) {
            if (position in imagesList.indices) {
                val imageFile = File(imagesList[position])
                if (imageFile.exists()) {
                    holder.binding?.let { binding ->
                        // Check if the activity is not destroyed before loading the image
                        if (holder.itemView.context is Activity && !(holder.itemView.context as Activity).isDestroyed) {
                            // Load image with Glide
                            Glide.with(holder.itemView.context)
                                .load(imageFile)
                                .into(binding.galleryItem)
                        }
                        // Check if the image is selected and update UI accordingly
                        val isSelected = selectedImages.contains(imagesList[position])
                        binding.viewCircle.isVisible = !isSelected
                        binding.viewSelectedImages.isVisible = isSelected
                        binding.tvCountSelectedImages.isVisible = isSelected
                        binding.tvCountSelectedImages.text =
                            (selectedImages.indexOf(imagesList[position]) + 1).toString()
                    }
                    // Calculate the width dynamically based on screen width and margins
                    val displayMetrics = holder.itemView.context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val screenHeight = displayMetrics.heightPixels
                    val imageWidth =
                        (screenWidth - dpToPx(
                            40,
                            holder.itemView.context
                        )) / 3 // Subtracting margins and dividing by 3 for 3 images in a row
                    val imageHeight = (screenHeight - dpToPx(40, holder.itemView.context)) / 5
                    holder.binding?.galleryItem?.layoutParams?.height = imageHeight
                    holder.binding?.galleryItem?.layoutParams?.width = imageWidth
                }

                holder.itemView.setSafeOnClickListener {
                    val imagePath = imagesList[position]
                    if (selectedImages.contains(imagePath)) {
                        selectedImages.remove(imagePath)
                    } else {
                        // If not selected, check if we can select more images
                        if (selectedImages.size < 5) {
                            selectedImages.add(imagePath)
                        } else {
                            // Notify the user that they can't select more images
                            Toast.makeText(
                                holder.itemView.context,
                                "You can select up to 5 images",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    notifyItemRangeChanged(0, itemCount)
                }
            }
        } else {
            holder.itemView.findViewById<ConstraintLayout>(R.id.btnOpenGallery)
                .setSafeOnClickListener {
                    itemClickListener.onGalleryPermissionDenied()
                }
        }
    }


    override fun getItemCount(): Int {
        return if (isGalleryGranted) {
            imagesList.size
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isGalleryGranted) VIEW_TYPE_IMAGE else VIEW_TYPE_GALLERY
    }

    inner class ViewHolder(itemView: View, val viewType: Int) : RecyclerView.ViewHolder(itemView) {
        val binding: PhotoLayoutBinding? = if (viewType == VIEW_TYPE_GALLERY) {
            PhotoLayoutBinding.bind(itemView)
        } else {
            null
        }
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    fun upGalleryValue(value: Boolean) {
        isGalleryGranted = value
        notifyItemRangeChanged(0, itemCount)
    }

    fun prefetchImages(context: Context, startIndex: Int, count: Int) {
        val endIndex = startIndex + count
        for (i in startIndex until endIndex) {
            if (i < imagesList.size) {
                Glide.with(context)
                    .load(imagesList[i])
                    .preload()
            }
        }
    }

    interface OnItemClickListener {
        //fun onItemClick(imagePath: String)
        fun onGalleryPermissionDenied()
    }

    fun getSelectedImages(): List<String> {
        return selectedImages
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedImages(selectedImages: List<String>) {
        Log.d("selected Images",selectedImages.size.toString())
        this.selectedImages.clear()
        this.selectedImages.addAll(selectedImages)
        //notifyDataSetChanged()
        notifyItemRangeChanged(0, itemCount)
    }

}

//fun previousSelectedImages(selectedImagePath: List<String>) {
//    previousSelectedImages.clear()
//    previousSelectedImages.addAll(selectedImages)
//    Log.d(TAG, "previouslyImages: $previousSelectedImages")
//}