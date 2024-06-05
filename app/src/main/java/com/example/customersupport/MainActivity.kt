package com.example.customersupport

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customersupport.databinding.ActivityMainBinding
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class MainActivity : AppCompatActivity(), BottomSheetDialogGallery.OnInputListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetDialogIssueType: BottomSheetDialogIssueType
    private lateinit var customBottomSheetDialog: BottomSheetDialogGallery
    private lateinit var imageAdapter: ImageSelectionAdapter
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
        ediTextIssueDescription()
        imageSelectionIssue()
        observeViewModel()
        binding.btnReview.setSafeOnClickListener {
            validateAndSubmitForm()
        }
    }

    private fun ediTextIssueDescription() {
        binding.editTextIssueDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    binding.tvErrorIssueDescription.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
        })
    }

    private fun observeViewModel() {
        supportViewModel.selectedIssue.observe(this, Observer { issue ->
            updateTextView(issue?.issueType)
        })
        supportViewModel.imageSelectedIssue.observe(this, Observer { image ->
            imageAdapter.updateImages(image)
            customBottomSheetDialog.updateSelectedImageGallery(image)
            updateUiVisibility(image)
        })
    }

    private fun bottomSheetDialogIssueInitialize() {
        //bottomSheetDialogIssueType = BottomSheetDialogIssueType(this, supportViewModel)
        bottomSheetDialogIssueType = BottomSheetDialogIssueType(this, supportViewModel.selectedPosition.value) { (issueText, position) ->
            supportViewModel.selectIssue(issueText?.let { IssueDataClass(it) })
            supportViewModel.selectPosition(position)
        }
        binding.tvIssueTypeDialog.setSafeOnClickListener {
            bottomSheetDialogIssueType.openIssueDialog(binding.tvIssueTypeDialog)
        }
    }

    fun updateTextView(issueText: String?) {
        binding.tvIssueTypeDialog.text = issueText ?: ""

        //Log.d("test for some field", "$issueText")
        if (issueText?.isNotEmpty() == true) {
            binding.tvErrorIssueType.visibility = View.GONE
        } else {
//            supportViewModel.updateIssue(null)
//            supportViewModel.selectIssue(null)
            supportViewModel.selectIssue(IssueDataClass("Select Issue Type"))
            supportViewModel.selectPosition(null)
            binding.tvErrorIssueType.visibility = View.VISIBLE
        }
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
        imageAdapter = ImageSelectionAdapter(supportViewModel, images,
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
                    //Log.d("current Image Size --", "${images.size}")
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
        binding.rvImage.adapter = imageAdapter
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
            imageAdapter.notifyDataSetChanged()
            if (imagePaths.isEmpty()) {
                binding.tvErrorAttachedImages.visibility = View.VISIBLE

            } else {
                binding.tvErrorAttachedImages.visibility = View.GONE
            }
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
            savePermissionIndex()
            checkPermissionAndOpenGallery()
        }
    }

    private fun validateAndSubmitForm() {
        val issueType = binding.tvIssueTypeDialog.text.toString()
        val issueDescription = binding.editTextIssueDescription.text.toString()
        val imagesAttached = images.size > 0

        if (issueType.isEmpty()) {
            binding.tvErrorIssueType.visibility = View.VISIBLE
        } else {
            binding.tvErrorIssueType.visibility = View.GONE
        }

        if (issueDescription.isEmpty()) {
            binding.tvErrorIssueDescription.visibility = View.VISIBLE
        } else {
            binding.tvErrorIssueDescription.visibility = View.GONE
        }

        if (!imagesAttached) {
            binding.tvErrorAttachedImages.visibility = View.VISIBLE
        } else {
            binding.tvErrorAttachedImages.visibility = View.GONE
        }

        if (issueType.isNotEmpty() && issueDescription.isNotEmpty() && imagesAttached) {

            showCustomDialog()



        }
    }

    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_box)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        //dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.custome_dialog_bg))
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.findViewById<Button>(R.id.btn_okay).setSafeOnClickListener {


            binding.tvIssueTypeDialog.text = ""
            supportViewModel.selectIssue(IssueDataClass(""))
            bottomSheetDialogIssueType.updateIssueText(null)
            supportViewModel.selectPosition(null)

            binding.editTextIssueDescription.text.clear()

            images.clear()
            imageAdapter.notifyDataSetChanged()
            customBottomSheetDialog.updateSelectedImageGallery(images)
            supportViewModel.updateImagesForIssue(images)

            binding.viewForImages.visibility = View.VISIBLE
            binding.tvImages.visibility = View.VISIBLE
            binding.tvImagesUpload.visibility = View.VISIBLE

            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()
    }
}

//            val newImages = imagePaths.map { ImageSelectionDataClass(it) }
//            adapter.updateImages(newImages)
//            adapter.notifyDataSetChanged()

//            Handler(Looper.getMainLooper()).postDelayed({
//                binding.tvErrorIssueType.visibility = View.GONE
//            }, 5000) // 2000 milliseconds = 2 seconds

//            Handler(Looper.getMainLooper()).postDelayed({
//                binding.tvErrorIssueDescription.visibility = View.GONE
//            }, 5000) // 2000 milliseconds = 2 seconds

//            Handler(Looper.getMainLooper()).postDelayed({
//                binding.tvErrorAttachedImages.visibility=View.GONE
//            },5000)

//binding.btnReview.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorEnabled)))
//dialog.window?.setLayout(
//ViewGroup.LayoutParams.WRAP_CONTENT,
//resources.getDimensionPixelSize(R.dimen.dp_287)
//)
