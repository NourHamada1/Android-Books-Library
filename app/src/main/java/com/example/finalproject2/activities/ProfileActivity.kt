package com.example.finalproject2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.finalproject2.R
import com.example.finalproject2.adapter.AdapterPdfFavorite
import com.example.finalproject2.app.MyApplication
import com.example.finalproject2.databinding.ActivityProfileBinding
import com.example.finalproject2.model.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold books
    private lateinit var booksArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadFavoriteBooks()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileEditBtn.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            startActivity(intent)
        }
    }


    private fun loadUserInfo() {
        //DB ref to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value ?: 0}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert timestamp format
                    val formattedDate =
                        if (timestamp.isEmpty()) "" else MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    //set image
                    try {
                        Glide.with(this@ProfileActivity).load(profileImage)
                            .placeholder(R.drawable.ic_person).into(binding.profileIv)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadFavoriteBooks() {
        //init arraylist
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear array list before add data
                    booksArrayList.clear()
                    for (ds in snapshot.children) {
                        //get only id of the books
                        val bookId = "${ds.child("bookId").value}"

                        //set to model
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        //add model to list
                        booksArrayList.add(modelPdf)
                    }
                    //set number of favorite books
                    binding.favoriteBookCountTv.text = "${booksArrayList.size}"

                    //setup adapter
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, booksArrayList)

                    //set adapter to recyclerview
                    binding.favoriteRv.adapter = adapterPdfFavorite

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}