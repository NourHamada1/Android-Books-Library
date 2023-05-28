package com.example.finalproject2.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.finalproject2.adapter.AdapterPdfAdmin
import com.example.finalproject2.databinding.ActivityPdfListAdminBinding
import com.example.finalproject2.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //category id, title
    private var categoryId = ""
    private var category = ""

    //array list to hold books
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //get from intent that we passed from adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set pdf category
        binding.subTitleTv.text = category

        //load pdf/books
        loadPdfList()

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //filter data
                try {
                    adapterPdfAdmin.filter!!.filter(s)

                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    private fun loadPdfList() {
        //init arrayList
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear List before start adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //add to list
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.e(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}