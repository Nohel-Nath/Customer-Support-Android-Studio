package com.example.customersupport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SupportViewModel : ViewModel() {
    private val images: MutableLiveData<List<String>> = MutableLiveData()
    private val selectedImages: MutableLiveData<List<String>> = MutableLiveData()
    private val _selectedIssue = MutableLiveData<IssueDataClass?>()
    val selectedIssue: LiveData<IssueDataClass?> get() = _selectedIssue
    private val _imagesSelectedIssue =
        MutableLiveData<MutableList<ImageSelectionDataClass>>(mutableListOf())
    val imageSelectedIssue: LiveData<MutableList<ImageSelectionDataClass>> get() = _imagesSelectedIssue

    fun updateImagesForIssue(newImages: List<ImageSelectionDataClass>) {
        _imagesSelectedIssue.value = newImages.toMutableList()
    }

    fun removeImage(position: Int) {
        _imagesSelectedIssue.value?.let {
            if (position >= 0 && position < it.size) {
                it.removeAt(position)
                _imagesSelectedIssue.value = it
            }
        }
    }

    init {
        images.value = emptyList()
        selectedImages.value = emptyList()
    }

    fun updateImages(newImages: List<String>) {
        images.value = newImages
    }

    fun selectIssue(issue: IssueDataClass?) {
        _selectedIssue.value = issue
    }
}
