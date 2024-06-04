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

class BottomSheetDialogIssueType(private val context: Context,private val issueViewModel: SupportViewModel) {
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
        adapter = IssueAdapter(selectedPosition, object : IssueAdapter.OnItemClickListener {
            override fun onClickItem(issueText: String?) {
                selectedPosition = adapter.getSelectedItem()
                // Update UI according to the selected item
                updateButtonColor(issueText)
                selectedPosition?.let {
                    adapter.notifyItemChanged(it)
                }
                issueViewModel.selectIssue(if (selectedPosition != null) IssueDataClass(issueText ?: "") else null)
            }
        }) // Pass selectedPosition here
        //selectedPosition=null
        val dummyData = listOf(
            IssueDataClass("Trading"),
            IssueDataClass("Deposit"),
            IssueDataClass("Withdrawal"),
            IssueDataClass("BO A/C creation"),
            IssueDataClass("Change BO A/C Information"),
            IssueDataClass("Wrong Information"),
            IssueDataClass("Others"),
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
        selectedPosition?.let { position ->
            binding.recycleViewIssueType.post {
                (binding.recycleViewIssueType.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(position, 0)
            }
        }
    }

    private fun updateButtonColor(issueText: String?) {
        if (issueText == null && selectedPosition == null) {
            binding.btnDone.setTextColor(Color.parseColor("#80ffffff"))
            binding.btnDone.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#090909"))
            Log.d("Message", "it works")
        } else {
            binding.btnDone.setTextColor(Color.parseColor("#090909"))
            binding.btnDone.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#00C806"))
            Log.d("Message", "it working")
        }
        binding.btnDone.setSafeOnClickListener{
            if (selectedPosition != null) {
                dialog?.dismiss()
            }
            if (selectedPosition != null && issueText != null) {
                (context as? MainActivity)?.updateTextView(issueText)
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateIssueText(issueText: String?) {
        // Update the issue text in your dialog UI here
        //do nothing
        selectedPosition = null
        adapter.notifyDataSetChanged() // Refresh entire dataset to clear selection

        updateButtonColor(null)
    }
}

//val issueList = resources.getStringArray(R.array.issue_name
//        data = bankList . map { SearchDataClass(it) }.sortedBy { it.item }
//        private fun setUpRecycleView() {
//    bankAdapter = SearchAdapter()
//    binding.rvSearch.adapter = bankAdapter
//    dataSetSize = bankDataList.size
//    bankAdapter.submitSearchClass(bankDataList)