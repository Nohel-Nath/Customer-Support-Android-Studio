package com.example.customersupport

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customersupport.databinding.BottomLayoutIssueTypeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetDialogIssueType(
    private val context: Context,
    private val issueViewModel: SupportViewModel
) {
    private lateinit var binding: BottomLayoutIssueTypeBinding
    private var dialog: BottomSheetDialog? = null
    private lateinit var adapter: IssueAdapter
    private var selectedPosition: Int? = null

    fun openIssueDialog(textView: TextView) {
        dialog = BottomSheetDialog(context)
        binding = BottomLayoutIssueTypeBinding.inflate(LayoutInflater.from(context))
        dialog?.setContentView(binding.root)
        val windowHeight = (context.resources.displayMetrics.heightPixels) * .6
        val layoutParams = binding.root.layoutParams
        layoutParams.height = windowHeight.toInt()
        binding.root.layoutParams = layoutParams
        dialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog?.behavior?.isDraggable = false
        dialog?.show()
        dialog?.setCancelable(false)
        adapter = IssueAdapter(issueViewModel.selectedPosition.value, object : IssueAdapter.OnItemClickListener {
            override fun onClickItem(issueText: String?) {
                selectedPosition = adapter.getSelectedItem()
                issueViewModel.selectPosition(selectedPosition)
                Log.d("test for some field for selected position", "$selectedPosition")
                // Update UI according to the selected item
                updateButtonColor(issueText)
                selectedPosition?.let {
                    adapter.notifyItemChanged(it)
                }
                issueViewModel.selectIssue(
                    if (selectedPosition != null) IssueDataClass(
                        issueText ?: ""
                    ) else null
                )
            }
        }) // Pass selectedPosition here
        //selectedPosition=null
        //issueViewModel.selectPosition(null)
        val dummyData = listOf(
            IssueDataClass("Hello 1"),
            IssueDataClass("Hello 1"),
            IssueDataClass("Hello 1"),
            IssueDataClass("Hello 1"),
            IssueDataClass("Hello 1"),
            IssueDataClass("Hello 1"),
            IssueDataClass("Hello 7"),
            IssueDataClass("Hello 8"),
            IssueDataClass("Hello 9"),
            IssueDataClass("Hello 10"),
            IssueDataClass("Hello 11"),
            IssueDataClass("Hello 12"),
            IssueDataClass("Hello 13"),
            IssueDataClass("Hello 14"),
            IssueDataClass("Hello 15"),
            IssueDataClass("Hello 16"),
            IssueDataClass("Hello 17"),
            IssueDataClass("Hello 18"),
            IssueDataClass("Hello 19"),
            IssueDataClass("Hello 20")
        )
        adapter.submitIssueClass(dummyData)
        binding.recycleViewIssueType.adapter = adapter
        updateButtonColor(null)
        issueViewModel.selectedPosition.value?.let { position ->
            binding.recycleViewIssueType.post {
                (binding.recycleViewIssueType.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(position, 0)
            }
        }
    }

    private fun updateButtonColor(issueText: String?) {
        if (issueText == null && issueViewModel.selectedPosition.value == null) {
            if(::binding.isInitialized) {
                binding.btnDone.setTextColor(Color.parseColor("#80ffffff"))
                binding.btnDone.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#090909"))
                Log.d("Message", "it works")
            }
        } else {
            if(::binding.isInitialized) {
                binding.btnDone.setTextColor(Color.parseColor("#090909"))
                binding.btnDone.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#00C806"))
                Log.d("Message", "it working")
            }
        }
        if(::binding.isInitialized) {
            binding.btnDone.setSafeOnClickListener {
                if (issueViewModel.selectedPosition.value != null) {
                    dialog?.dismiss()
                }
                if (issueViewModel.selectedPosition.value != null && issueText != null) {
                    (context as? MainActivity)?.updateTextView(issueText)
                }

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateIssueText(issueText: String?) {
        selectedPosition = null
        if (::adapter.isInitialized){
            adapter.notifyDataSetChanged()
        }
        if(::binding.isInitialized) {
            updateButtonColor(null)
        }
    }
}

//val issueList = resources.getStringArray(R.array.issue_name
//        data = bankList . map { SearchDataClass(it) }.sortedBy { it.item }
//        private fun setUpRecycleView() {
//    bankAdapter = SearchAdapter()
//    binding.rvSearch.adapter = bankAdapter
//    dataSetSize = bankDataList.size
//    bankAdapter.submitSearchClass(bankDataList)