package com.example.finalproject2.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.finalproject2.R
import com.example.finalproject2.app.MyApplication
import com.example.finalproject2.databinding.ActivityPdfDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailsBinding

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //book id
    private var bookId = ""

    //get from firebase
    private var bookTitle = ""
    private var bookUrl = ""

    // boolean to check is in favorite or not
    private var isInMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        // init progressBar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, open pdfViewActivity
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        binding.BuyBookBtn.setOnClickListener {
            MyApplication.incrementBookSalesCount(bookId)

        }

        //handle click , add/remove favorite
        binding.favoriteBtn.setOnClickListener {
            if (isInMyFavorite){
                //already in fav -> Remove
                MyApplication.removeFromFavorite(this, bookId)
            }
            else{
                //not in fav -> Add
                addToFavorite()
            }
        }
    }


    private fun loadBookDetails() {
        //Books > bookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val salesCount = "${snapshot.child("salesCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load pdf category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    //load pdf thumbnail, pages count
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )

                    //load Pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //set data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.salesTv.text = salesCount
                    binding.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private fun checkIsFavorite() {
        Log.e(TAG, "checkIsFavorite: Checking if book in favorite or not")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
                        //In Favorite

                        Log.e(TAG, "onDataChange: Available in favorite")
                        //set drawable to top icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_filled,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = "Remove Favorite"

                    } else {
                        Log.e(TAG, "onDataChange: Not Available in favorite")
                        //Not in favorite
                        //  set drawable to top icon
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite() {
        Log.e(TAG, "addToFavorite: Adding to favorite")
        val timestamp = System.currentTimeMillis()

        // setup data to add to DB
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to DB
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //added to favorite
                Log.e(TAG, "addToFavorite: Added to favorite")
                Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //failed to add to favorite
                Log.e(TAG, "addToFavorite: Failed to add to favorite due  to ${e.message}")
                Toast.makeText(
                    this,
                    "Failed to add to favorite due  to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }


}