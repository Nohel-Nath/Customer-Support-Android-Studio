package com.example.customersupport

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val horizontalSpaceWidth: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        // Add the specified space to the right of every item except the last one
        if (position != parent.adapter?.itemCount?.minus(1)) {
            outRect.right = horizontalSpaceWidth
        }
    }
}