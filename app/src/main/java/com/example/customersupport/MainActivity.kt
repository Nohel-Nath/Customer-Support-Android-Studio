package com.example.customersupport

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customersupport.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), BottomSheetDialogGallery.OnInputListener {
    //this is check for newText branch
    // var l=5

    // var m=10
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetDialogIssueType: BottomSheetDialogIssueType
    private lateinit var customBottomSheetDialog: BottomSheetDialogGallery
    private lateinit var adapter: ImageSelectionAdapter
    private val images = mutableListOf<ImageSelectionDataClass>()
    private var currentPermissionIndex = 0
    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    private val permissions = arrayOf(
        storagePermission
    )
    private val supportViewModel: SupportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.show()
        customBottomSheetDialog = BottomSheetDialogGallery(this)
        customBottomSheetDialog.setOnInputListener(this)
        bottomSheetDialogIssueInitialize()
        currentPermissionIndex = getSavedPermissionIndex()
        maskGroupingHeight()
        binding.viewForImages.setSafeOnClickListener {
            checkPermissionAndOpenGallery()
        }
        imageSelectionIssue()
        observeViewModel()
    }

    private fun observeViewModel() {
        supportViewModel.selectedIssue.observe(this, Observer { issue ->
            updateTextView(issue?.issueType)
        })
        supportViewModel.imageSelectedIssue.observe(this, Observer { image ->
            adapter.updateImages(image)
            customBottomSheetDialog.updateSelectedImageGallery(image)
            updateUiVisibility(image)
        })
    }

    private fun bottomSheetDialogIssueInitialize() {
        bottomSheetDialogIssueType = BottomSheetDialogIssueType(this, supportViewModel)
        binding.tvIssueTypeDialog.setSafeOnClickListener {
            bottomSheetDialogIssueType.openIssueDialog(binding.tvIssueTypeDialog)
        }
    }

    fun updateTextView(issueText: String?) {
        binding.tvIssueTypeDialog.text = issueText ?: ""
    }

    private fun maskGroupingHeight() {
        val parentLayout =
            findViewById<View>(android.R.id.content) // or any other parent view you have
        val parentHeight = parentLayout.height
        val layoutParams = binding.newConstraint.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topToTop = parentHeight
        layoutParams.bottomToBottom = binding.cardView.id
        binding.newConstraint.layoutParams = layoutParams
    }

    private fun imageSelectionIssue() {
        binding.rvImage.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ImageSelectionAdapter(supportViewModel, images,
            object : ImageSelectionAdapter.OnItemClickListener {
                override fun onItemClick() {
                    customBottomSheetDialog.openCameraGalleryDialog()
                }

                override fun changeUi() {
                    binding.viewForImages.visibility = View.VISIBLE
                    binding.tvImages.visibility = View.VISIBLE
                    binding.tvImagesUpload.visibility = View.VISIBLE
                }

                override fun updatedImg(images: MutableList<ImageSelectionDataClass>) {
                    customBottomSheetDialog.updateSelectedImageGallery(images)
                    Log.d("current Image Size --", "${images.size}")
                }

                override fun sendSingleImage(
                    imageResId: String,
                    imageView: ImageView,
                    imageName: String
                ) {
                    val intent = Intent(this@MainActivity, SingleImageShowing::class.java).apply {
                        putExtra("IMAGE_URL", imageResId)
                        putExtra("IMAGE_NAME", imageName)
                    }
                    val option = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@MainActivity,
                        Pair(imageView, "picked_image")
                    )
                    startActivity(intent, option.toBundle())
                }
            })
        binding.rvImage.adapter = adapter
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.dp_4) // Convert dp to pixels
        binding.rvImage.addItemDecoration(HorizontalSpaceItemDecoration(spacingInPixels))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun sendInput(imagePaths: List<String>) {
        if (imagePaths.isNotEmpty()) {
            val newImages = imagePaths.map {
                val imageName =
                    extractImageNameFromPath(it) // Implement this method to extract name from path
                ImageSelectionDataClass(it, imageName)
            }
            supportViewModel.updateImagesForIssue(newImages)
            //adapter.updateImages(newImages)
            adapter.notifyDataSetChanged()
            updateUiVisibility(newImages)
        } else {
            updateUiVisibility(emptyList())
        }
    }

    private fun updateUiVisibility(imageList: List<ImageSelectionDataClass>) {
        if (imageList.isNotEmpty()) {
            binding.viewForImages.visibility = View.INVISIBLE
            binding.tvImages.visibility = View.INVISIBLE
            binding.tvImagesUpload.visibility = View.INVISIBLE
        } else {
            binding.viewForImages.visibility = View.VISIBLE
            binding.tvImages.visibility = View.VISIBLE
            binding.tvImagesUpload.visibility = View.VISIBLE
        }
    }

    private fun extractImageNameFromPath(path: String): String {
        return File(path).name
    }

    private fun getSavedPermissionIndex(): Int {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        return prefs.getInt("currentPermissionIndex", 0)
    }

    private fun savePermissionIndex() {
        val prefs = getPreferences(Context.MODE_PRIVATE)
        prefs.edit().putInt("currentPermissionIndex", currentPermissionIndex).apply()
    }

    private fun checkPermissionAndOpenGallery() {
        if (currentPermissionIndex < permissions.size) {
            val permission = permissions[currentPermissionIndex]
            requestPermissions(arrayOf(permission), currentPermissionIndex)
        } else {
            Log.d("permission index ", "open gallery")
            // currentUploadType?.let { customBottomSheetDialog.openCameraGalleryDialog(it) }
            customBottomSheetDialog.openCameraGalleryDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        customBottomSheetDialog.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == currentPermissionIndex) {
            currentPermissionIndex++
            savePermissionIndex() // Save the updated permission index
            checkPermissionAndOpenGallery()
        }
    }
}
//            val newImages = imagePaths.map { ImageSelectionDataClass(it) }
//            adapter.updateImages(newImages)
//            adapter.notifyDataSetChanged()
