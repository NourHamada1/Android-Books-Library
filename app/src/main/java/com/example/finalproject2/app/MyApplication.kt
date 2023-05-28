package com.example.finalproject2.app

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.finalproject2.Constants
import com.example.finalproject2.activities.PdfDetailsActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            //format dd/MM/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //function to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            //using url we can get file and its metadata from firebase storage

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    Log.e(TAG, "loadPdfSize: got metadata")

                    val bytes = storageMetaData.sizeBytes.toDouble()

                    Log.e(TAG, "loadPdfSize: Size Bytes $bytes")

                    //convert bytes to KB/MB
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb > 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"

                    }

                }
                .addOnFailureListener {
                    Log.e(TAG, "loadPdfSize: Failed to get metadata")

                }

        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {

            val TAG = "PDF_THUMBNAIL_TAG"

            //using url we can get file and its metadata from firebase storage

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    Log.e(TAG, "loadPdfSize: Size Bytes $bytes")

                    // set tp pdfview
                    pdfView.fromBytes(bytes)
                        .pages(0) // show first page only
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.e(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.e(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.e(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                            //pdf loaded, we can set page count, pdf thumbnail
                            progressBar.visibility = View.INVISIBLE

                            //if pages param is not null then set page numbers
                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()

                }
                .addOnFailureListener {
                    Log.e(TAG, "loadPdfSize: Failed to get metadata")

                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            //load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category: String = "${snapshot.child("category").value}"

                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            val TAG = "DELETE_BOOK_TAG"

            Log.e(TAG, "deleteBook: Deleting...")

            //progress Dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.e(TAG, "deleteBook: Deleting from storage")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.e(TAG, "deleteBook: Deleted from storage successfully")
                    Log.e(TAG, "deleteBook: Deleting from DB now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Book deleted Successfully", Toast.LENGTH_SHORT)
                                .show()
                            Log.e(TAG, "deleteBook: Deleted from DB Successfully")

                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Log.e(TAG, "deleteBook: Failed to delete book from DB")
                            Toast.makeText(
                                context,
                                "Failed to delete book from DB",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Log.e(TAG, "deleteBook: Failed to delete book from storage")
                    Toast.makeText(
                        context,
                        "Failed to delete book from storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            //1- get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount == "null") {
                            viewsCount = "0"
                        }

                        //2- increment views count
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data tp update in DB
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to DB
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun incrementBookSalesCount(bookId: String) {
            //1- get current book Sales count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var salesCount = "${snapshot.child("salesCount").value}"

                        if (salesCount == "" || salesCount == "null") {
                            salesCount = "0"
                        }

                        //2- increment views count
                        val newSalesCount = salesCount.toLong() + 1

                        //setup data tp update in DB
                        val hashMap = HashMap<String, Any>()
                        hashMap["salesCount"] = newSalesCount

                        //set to DB
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        public fun removeFromFavorite(context: Context, bookId: String) {
            val TAG = "REMOVE_FAV_TAG"
            Log.e(TAG, "removeFromFavorite: Removing from favorite")

            val firebaseAuth = FirebaseAuth.getInstance()

            //database ref
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Removed from favorite", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "removeFromFavorite: Failed to remove from favorite due to ${e.message}")

                    Toast.makeText(
                        context,
                        "Failed to remove from favorite due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
        }


    }


}