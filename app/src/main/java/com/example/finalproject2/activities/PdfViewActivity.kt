package com.example.finalproject2.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.finalproject2.Constants
import com.example.finalproject2.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding

    //TAG
    private companion object {
        const val TAG = "PDF_VIEW_TAG"
    }

    //book id
    var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent, to load book from firebase
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadBookDetails() {
        Log.e(TAG, "loadBookDetails: Get Pdf URL from DB")
        //DB Reference to get book details

        //Get book Url using BookId
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book url
                    val pdfUrl = snapshot.child("url").value
                    Log.e(TAG, "onDataChange: PDF_URL = $pdfUrl")

                    //load pdf using url from firebase storage
                    loadBookFromUrl("$pdfUrl")

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.e(TAG, "loadBookFromUrl: Get Pdf from firebase storage")

        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.e(TAG, "loadBookFromUrl: pdf got from url Successfully")

                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)// false = scroll vertical
                    .onPageChange {page, pageCount ->
                        //set current and total pages in toolbar subtitle
                        val currentPage = page+1 // (+1) because page starts from 0
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"

                        Log.e(TAG, "loadBookFromUrl: $currentPage/$pageCount")

                    }
                    .onError { t->
                        Log.e(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.e(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE

            }
            .addOnFailureListener{
                Log.e(TAG, "loadBookFromUrl: Failed to get Book url")
                binding.progressBar.visibility = View.GONE

            }
    }
}