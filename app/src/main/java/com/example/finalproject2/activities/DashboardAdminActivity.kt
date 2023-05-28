package com.example.finalproject2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.finalproject2.ModelCategory
import com.example.finalproject2.adapter.AdapterCategory
import com.example.finalproject2.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    private lateinit var firebaseAuth: FirebaseAuth

    //array list to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryArrayList = ArrayList()

        // initialize firebase
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        // search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        // handle click logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //handle click, start add category page

        binding.addCategoryBtn.setOnClickListener {
            val i = Intent(this, CategoryAddActivity::class.java)
            startActivity(i)
        }

        //handle click, start add pdf page
        binding.addPdfB.setOnClickListener {
            val i = Intent(this, PdfAddActivity::class.java)
            startActivity(i)
        }

        // handle click open profile
        binding.profileBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadCategories() {
//        initialize arraylist
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before start adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    //add to array list
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
                //set adapter to recycler view
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in -> move to main screen
            val i = Intent(this@DashboardAdminActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
            //logged in -> go to user dashboard screen
            val email = firebaseUser.email

            // set to text view of toolbar
            binding.subTitleTv.text = email

        }
    }
}