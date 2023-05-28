package com.example.finalproject2.filters

import android.widget.Filter
import com.example.finalproject2.adapter.AdapterPdfAdmin
import com.example.finalproject2.model.ModelPdf

//used to filter data from recyclerview | search pdf from pdf list in recyclerview

class FilterAdminPdf : Filter {
    //array list to search
    var filterList: ArrayList<ModelPdf>

    // adapter for filter
    var adapterPdfAdmin: AdapterPdfAdmin

    // constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        //value to be searched should not be empty or null
        if (constraint != null && constraint.isNotEmpty()) {
            //change to upper case , lower case (avoid case sensitivity)
            var filterModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                // validate if match
                if (filterList[i].title.lowercase().contains(constraint)){
                    //searched value is similar to value in list, add to filtered list
                    filterModels.add(filterList[i])
                }
            }
            results.count = filterModels.size
            results.values = filterModels
        } else{
            //searched value is either null or empty, return all data
            results.count= filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>
        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }
}