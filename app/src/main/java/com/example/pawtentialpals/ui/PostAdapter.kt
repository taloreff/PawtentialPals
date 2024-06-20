package com.example.pawtentialpals.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(
    dataList: List<Post>,
    private val onClickListener: View.OnClickListener
) : ListAdapter<DataObject, DataObject>(
    ListItemAdapterDataObserver(dataList, onClickListener)
) {

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ): Int {
        return holder.onBindViewHolder(position)
    }
}