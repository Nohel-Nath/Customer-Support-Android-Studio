package com.example.customersupport

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customersupport.databinding.BottomlayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

@SuppressLint("NotifyDataSetChanged")
class BottomSheetDialogGallery(private val context: Context) {
    private lateinit var binding: BottomlayoutBinding
    private lateinit var recyclerView: RecyclerView
    private var images: ArrayList<String> = ArrayList()
    private lateinit var adapter: GalleryAdapter
    private lateinit var manager: GridLayoutManager
    private val galleryHelper: GalleryHelper = GalleryHelper(context)
    private var previouslySelectedImages = mutableListOf<String>()
    private lateinit var galleryViewModel: SupportViewModel

    companion object {
        private const val REQUEST_GALLERY_PERMISSION = 100
    }

    private var selectedAlbum: String? = null
    private var albumsDialog: BottomSheetDialog? = null
    private var dialog: BottomSheetDialog? = null
    private lateinit var albumsList: List<Album>
    val paddingInDp = 5

    interface OnInputListener {
        fun sendInput(imagePaths: List<String>)
    }

    private var mOnInputListener: OnInputListener? = null
    fun setOnInputListener(listener: OnInputListener) {
        mOnInputListener = listener
    }

    private fun sendSelectedImagePath(imagePaths: List<String>) {
        mOnInputListener?.sendInput(imagePaths)
    }

    fun updateSelectedImageGallery(images: MutableList<ImageSelectionDataClass>) {
        // Clear previously selected images
        previouslySelectedImages.clear()
        // Add new images
        previouslySelectedImages.addAll(images.map { it.imageResId })
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }

    fun openCameraGalleryDialog() {
        galleryViewModel =
            ViewModelProvider(context as ViewModelStoreOwner)[SupportViewModel::class.java]
        Log.d("images size :", images.size.toString())
        dialog = BottomSheetDialog(context)
        binding = BottomlayoutBinding.inflate(LayoutInflater.from(context))
        dialog?.setContentView(binding.root)
        dialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog?.behavior?.isDraggable = false
        binding.imageViewCancel.setSafeOnClickListener {
            dialog?.dismiss()
        }
        binding.btnDone.setSafeOnClickListener {
            if (adapter.getSelectedImages().isEmpty()) {
                Toast.makeText(context, "No Images Selected", Toast.LENGTH_LONG).show()
            } else {
                val selectedImagePath = adapter.getSelectedImages()
                previouslySelectedImages.clear()
                previouslySelectedImages.addAll(selectedImagePath)
                sendSelectedImagePath(selectedImagePath)
                Log.d(TAG, "my image : $selectedImagePath")
                val message = "Selected Images:\n\n${selectedImagePath.joinToString("\n\n")}"
                dialog?.dismiss()
            }
        }
        recyclerView = binding.galleryRecycler
        this.images = ArrayList()
        val storagePermissionForCameraGallery =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        val isHaveGalleryPermission = ContextCompat.checkSelfPermission(
            context,
            storagePermissionForCameraGallery
        ) == PackageManager.PERMISSION_GRANTED
        adapter = GalleryAdapter(
            galleryViewModel,
            isHaveGalleryPermission,
            object : GalleryAdapter.OnItemClickListener {
                override fun onGalleryPermissionDenied() {
                    requestGalleryPermission()
                }
            })
        recyclerView.itemAnimator = null
        recyclerView.adapter = adapter
        adapter.updateData(this.images)
        adapter.setSelectedImages(previouslySelectedImages)
        galleryAdapterSet()
        selectedAlbum = null
        albumsList = emptyList()
        if (isHaveGalleryPermission) {
            binding.imageViewCancel.visibility = View.VISIBLE
            binding.btnDone.visibility = View.VISIBLE
            this.images.addAll(galleryHelper.loadImages(selectedAlbum))
            galleryViewModel.updateImages(images)
            adapter.notifyDataSetChanged()
            albumsList = galleryHelper.fetchAlbums()
            binding.viewForAlbumSelection.setSafeOnClickListener {
                showAlbumsDialog()
            }
        }
        dialog?.setCancelable(true)
        dialog?.show()
    }

    private fun galleryAdapterSet() {
        manager = GridLayoutManager(context, 3)
        val itemDecoration = GridSpacingItemDecoration(dpToPx(paddingInDp))
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.layoutManager = manager
        val startIndex = 0 // Starting index of items to prefetch
        val count = 20 // Number of items to prefetch
        adapter.prefetchImages(context, startIndex, count)
    }

    @SuppressLint("InflateParams")
    private fun showAlbumsDialog() {
        val windowHeight = context.resources.displayMetrics.heightPixels
        albumsDialog = BottomSheetDialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogalbums, null)
        albumsDialog?.setContentView(dialogView)
        val layoutParams = dialogView.layoutParams
        layoutParams.height = windowHeight.toInt()
        dialogView.layoutParams = layoutParams
        val recyclerView = albumsDialog?.findViewById<RecyclerView>(R.id.recyclerViewForAlbum)
        val layoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = layoutManager
        val adapterAlbum = AlbumAdapter(object : AlbumAdapter.OnAlbumClickListener {
            override fun onAlbumClick(album: Album) {
                albumsDialog?.dismiss()
                selectedAlbum = album.albumName
                images.clear()
                images.addAll(galleryHelper.loadImages(selectedAlbum))
                galleryViewModel.updateImages(images)
                // Find the position of the selected album
                val position = albumsList.indexOfFirst { it.albumName == selectedAlbum }
                Log.d(TAG, "Position of Album is: $position")
                if (position != -1) {
                    adapter.notifyItemChanged(position)
                }
//                if (position != -1) {
//                    val newList= albumsList.toMutableList()
//                    // Remove the item
//                    val removedAlbum = newList.removeAt(position)
//                    // Add the item back to the beginning of the list
//                    newList.add(0, removedAlbum)
//                    // Notify the adapter about the changes
//                    adapter.notifyItemRemoved(position)
//                    adapter.notifyItemInserted(0)
//                    adapter.notifyItemRangeInserted(0, position)
//                }
                //adapter.notifyDataSetChanged()
                binding.tvAlbumName.text = selectedAlbum
                adapter.updateAlbumSelection(selectedAlbum)
                binding.galleryRecycler.scrollToPosition(0)
            }
        })
        recyclerView?.adapter = adapterAlbum
        adapterAlbum.submitList(albumsList)
        albumsDialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        albumsDialog?.behavior?.isDraggable = false
        albumsDialog?.show()
        albumsDialog?.setCancelable(true)
    }

    private fun requestGalleryPermission() {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(
                context,
                storagePermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            binding.imageViewCancel.visibility = View.VISIBLE
            binding.btnDone.visibility = View.VISIBLE
            adapter.upGalleryValue(true)
            albumsList = galleryHelper.fetchAlbums()
            binding.viewForAlbumSelection.setSafeOnClickListener {
                showAlbumsDialog()
            }
            //images.clear()
            images.addAll(galleryHelper.loadImages(selectedAlbum))
            galleryViewModel.updateImages(images)
            adapter.notifyDataSetChanged()
        } else {
            when {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    storagePermission
                ) -> {
                    showPermissionRationaleDialogForGallery()
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(storagePermission),
                        REQUEST_GALLERY_PERMISSION
                    )
                }
            }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_GALLERY_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    adapter.upGalleryValue(true)
                    //binding.iconImageView.visibility = View.VISIBLE
                    binding.imageViewCancel.visibility = View.VISIBLE
                    binding.btnDone.visibility = View.VISIBLE
                    albumsList = galleryHelper.fetchAlbums()
                    binding.viewForAlbumSelection.setSafeOnClickListener {
                        showAlbumsDialog()
                    }
                    //images.clear()
                    images.addAll(galleryHelper.loadImages(selectedAlbum))
                    galleryViewModel.updateImages(images)
                    adapter.notifyDataSetChanged()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity,
                            permissions[0]
                        )
                    ) {
                        explainFeatureUnavailableForGallery()
                    } else {
                        showPermissionRationaleDialogForGallery()
                    }
                }
            }
        }
    }

    private fun explainFeatureUnavailableForGallery() {
        AlertDialog.Builder(context)
            .setTitle("Feature Unavailable")
            .setMessage("The Image selection feature is currently unavailable because the storage permission has been denied.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.setNegativeButton("Open Settings") { _, _ ->
                openAppSettings()
            }.show()
    }

    private fun showPermissionRationaleDialogForGallery() {
        AlertDialog.Builder(context)
            .setTitle("Storage Permission")
            .setMessage("Storage permission is needed in order to select photo")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    ),
                    REQUEST_GALLERY_PERMISSION
                )
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
        dialog?.dismiss()
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

/*
galleryViewModel =
            ViewModelProvider(context as ViewModelStoreOwner)[GalleryViewModel::class.java]
 */