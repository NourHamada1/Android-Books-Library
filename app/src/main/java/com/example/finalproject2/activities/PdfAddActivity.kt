package com.example.finalproject2.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.finalproject2.ModelCategory
import com.example.finalproject2.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBinding

    //firebase authentication
    private lateinit var firebaseAuth: FirebaseAuth

    //progress Dialog (while uploading)
    private lateinit var progressDialog: ProgressDialog

    //array list to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>


    //uri of picked pdf
    private var pdfUri: Uri? = null

    //TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait..")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click , go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }
        // handle click , pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        //handle click ,  start uploading pdf/book
        binding.submitBtn.setOnClickListener {
            // 1- Validate Data
            // 2- Upload pdf to firebase storage
            // 3- Get url of uploaded pdf
            // 4- Upload pdf info to firebase db

            validatedata()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validatedata() {
        //Validate Data
        Log.e(TAG, "validateData: Validating data")

        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Title", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick PDF", Toast.LENGTH_SHORT).show()
        } else {
            //data validate , being upload
            uploadPdfToStorage()
        }


    }

    private fun uploadPdfToStorage() {
        //upload pdf to firebase storage
        Log.e(TAG, "uploadPdfToStorage: uploading to storage...")

        //show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //path of PDF in firebase storage
        val filePathAndName = "Books/$timestamp"

        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.e(TAG, "uploadPdfToStorage: PDF uploaded successfully, Getting Url...")

                //get url of uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)

            }
            .addOnFailureListener {
                Log.e(TAG, "uploadPdfToStorage: failed to upload PDF/Book")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed To Upload PDF/Book", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String?, timestamp: Long) {
        Log.e(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading Pdf Info...")

        //uid of current user
        val uid = firebaseAuth.uid

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["salesCount"] = 0

        //DB reference DB > Books > BookId > (Book Info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.e(TAG, "uploadPdfInfoToDb: uploaded to db successfully")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded..", Toast.LENGTH_SHORT).show()
                pdfUri = null

            }
            .addOnFailureListener {
                Log.e(TAG, "uploadPdfInfoToDb: failed to upload PDF/Book")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed To Upload PDF/Book", Toast.LENGTH_SHORT).show()

            }
    }


    private fun loadPdfCategories() {
        Log.e(TAG, "Load Categories: Loading pdf categories")
        //init array list
        categoryArrayList = ArrayList()

        //DB reference to load categories DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before add data
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // get data
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to array list
                    categoryArrayList.add(model!!)
                    Log.e(TAG, "onDataChange ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.e(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                //handle item click
                //get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                //set category to Text View
                binding.categoryTv.text = selectedCategoryTitle

                Log.e(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.e(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")

            }.show()
    }

    private fun pdfPickIntent() {
        Log.e(TAG, "pdfPickIntent: starting pdf pick intent")
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.e(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                Log.e(TAG, "PDF Pick Cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}