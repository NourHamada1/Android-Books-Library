package com.example.finalproject2.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.finalproject2.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfEditBinding

    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }

    private var bookId = ""

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //array list to hold category titles
    private lateinit var categoryTitleArrayList: ArrayList<String>

    //array list to hold category ids
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get book id to edit book Info
        bookId = intent.getStringExtra("bookId")!!

        // setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //handle click go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        // handle click pick category
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        //handle click being update
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private fun loadBookInfo() {
        Log.e(TAG, "loadBookInfo: Loading book info")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    // set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //load book category info using category id
                    Log.e(TAG, "onDataChange: Loading book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // get category
                                val category = snapshot.child("category").value
                                // set to text view
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var title = ""
    private var description = ""
    private fun validateData() {
        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        // validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.e(TAG, "updatePdf: Start updating pdf info...")

        //show progress
        progressDialog.setMessage("Updating Book Info")
        progressDialog.show()

        //setup data to update to DB
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.e(TAG, "updatePdf: Updated Successfully")
                Toast.makeText(this, "Updated Successfully..", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e(TAG, "updatePdf: Failed to update book")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update Book", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        // dialog to pick category of pdf/book

        //make string array from array list of string
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) { dialog, position ->
                // handle click, save clicked categories id and title
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                // set to textview
                binding.categoryTv.text = selectedCategoryTitle

            }.show()

    }

    private fun loadCategories() {
        Log.e(TAG, "loadCategories: loading categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before start adding data into
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.e(TAG, "onDataChange: Category Id $id")
                    Log.e(TAG, "onDataChange: Category $category")
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}