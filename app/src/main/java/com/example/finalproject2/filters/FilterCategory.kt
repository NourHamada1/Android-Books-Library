package com.example.finalproject2.filters

import android.widget.Filter
import com.example.finalproject2.ModelCategory
import com.example.finalproject2.adapter.AdapterCategory

class FilterCategory : Filter {

    private var filterList: ArrayList<ModelCategory>

    private var adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModels: ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].category.uppercase().contains(constraint)) {
                    filterModels.add(filterList[i])
                }
            }
            results.count = filterModels.size
            results.values = filterModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        adapterCategory.notifyDataSetChanged()
    }
}