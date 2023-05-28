package com.example.finalproject2.model

import com.example.finalproject2.adapter.AdapterPdfFavorite

class ModelPdf {

    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var categoryId: String = ""
    var url: String = ""
    var timestamp: Long = 0
    var viewsCount: Long = 0
    var salesCount: Long = 0
    var isFavorite = false

    //empty constructor (required by firebase)
    constructor()

    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        salesCount: Long,
        isFavorite: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.salesCount = salesCount
        this.isFavorite = isFavorite
    }


    //parameterized constructor

}