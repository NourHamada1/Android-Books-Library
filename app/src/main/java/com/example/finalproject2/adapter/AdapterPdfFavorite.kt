package com.example.finalproject2.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject2.activities.PdfDetailsActivity
import com.example.finalproject2.app.MyApplication
import com.example.finalproject2.databinding.RowPdfFavoriteBinding
import com.example.finalproject2.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    //    Context
    private val context: Context

    //Arraylist to hold Books
    private val booksArrayList: ArrayList<ModelPdf>

    //viewBinding
    private lateinit var binding: RowPdfFavoriteBinding

    constructor(context: Context, booksArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfFavorite(binding.root)
    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        //get data
        val model = booksArrayList[position]


        loadBookDetails(model, holder)

        //handle click, open pdf details page , pass book id to load details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", model.id) //pass book id mot category id
            context.startActivity(intent)
        }

        //handle click, remove from favorite
        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }
    }

    private fun loadBookDetails(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val salesCount = "${snapshot.child("salesCount").value ?: 0}"
                    val timestamp = "${snapshot.child("timestamp").value ?: 0}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount ").value ?: 0}"

                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.salesCount = salesCount.toLong()

                    //format date
                    val formattedDate =
                        if (timestamp.isEmpty()) "" else MyApplication.formatTimeStamp(timestamp.toLong())


                    MyApplication.loadCategory("$categoryId", holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$url",
                        "$title",
                        holder.pdfView,
                        holder.progressBar,
                        null
                    )
                    MyApplication.loadPdfSize("$url", "$title", holder.sizeTv)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = formattedDate

                }

                override fun onCancelled(error: DatabaseError) {


                }
            })
    }


    //constructor
    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init UI views
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavBtn = binding.removeFavBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv

    }


}