package com.example.pawtentialpals.ui

class ListItemAdapterDataObserver(
    private val dataList: List<DataObject>,
    private val onClickListener: OnItemClickListener
) : ListItem(_, dataList) {

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ): Int {
        return holder.onBindView(position, dataList[position])
    }

    override fun onAttachedToCustomViewHolder(
        holder: CustomDataViewHolder,
        position: Int
    ) {
        holder.onAttachViewComponent(position, dataList[position])
    }
}