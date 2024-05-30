package com.example.customersupport

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.customersupport.databinding.IssueLayoutBinding

class IssueAdapter(
    private var selectedPosition: Int? = null,
    private val itemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {
    inner class IssueViewHolder(val binding: IssueLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemText: IssueDataClass) {
            binding.apply {
                tvIssue.text = itemText.issueType
            }
        }
    }
    private val differCallBack = object : DiffUtil.ItemCallback<IssueDataClass>() {
        override fun areItemsTheSame(oldItem: IssueDataClass, newItem: IssueDataClass): Boolean {
            return oldItem.issueType == newItem.issueType
        }
        override fun areContentsTheSame(
            oldItem: IssueDataClass,
            newItem: IssueDataClass,
        ): Boolean {
            return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this, differCallBack)
    fun submitIssueClass(itemList: List<IssueDataClass>) {
        return differ.submitList(itemList)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): IssueAdapter.IssueViewHolder {
        val layoutInflate = LayoutInflater.from(parent.context)
        val binding = IssueLayoutBinding.inflate(layoutInflate, parent, false)
        return IssueViewHolder(binding)
    }
    override fun onBindViewHolder(
        holder: IssueAdapter.IssueViewHolder,
        @SuppressLint("RecyclerView") position: Int,
    ) {
        val item = differ.currentList[position]
        holder.bind(item)
        if (selectedPosition == position) {
            holder.binding.imageViewSelect.visibility = View.VISIBLE
        } else {
            holder.binding.imageViewSelect.visibility = View.INVISIBLE
        }
        holder.itemView.setSafeOnClickListener{
            // If the clicked item is already selected, deselect it
            if (selectedPosition == position) {
                selectedPosition = null
                itemClickListener.onClickItem(null)
            } else {
                // Deselect the previously selected item if any
                val previousSelectedPos = selectedPosition
                if (previousSelectedPos != null) {
                    selectedPosition = null
                    notifyItemChanged(previousSelectedPos)
                }
                // Select the clicked item and update the view
                selectedPosition = position
                itemClickListener.onClickItem(item.issueType)
            }
            // Update the view to reflect the selection/deselection
            notifyItemChanged(position)
        }
        holder.binding.viewForIssue.visibility =
            if (position == differ.currentList.size - 1) View.GONE else View.VISIBLE
    }
    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    interface OnItemClickListener {
        fun onClickItem(issueText: String?)
    }
    fun getSelectedItem(): Int? {
        return selectedPosition
    }
}

//fun getCurrentList(): List<IssueDataClass> {
//    return differ.currentList
//}
