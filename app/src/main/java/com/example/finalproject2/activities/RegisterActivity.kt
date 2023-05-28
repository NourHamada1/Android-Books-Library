package com.example.finalproject2.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.finalproject2.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    //firebase authentication

    private lateinit var firebaseAuth: FirebaseAuth

    //progress Dialog

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize firebase authentication

        firebaseAuth = FirebaseAuth.getInstance()

        // initialize progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed() // go to previous screen
        }

        binding.registerBtn.setOnClickListener {
            /* steps
            * 1- Input Data
            * 2- Validate Data
            * 3-Create Account - Firebase
            * 4-Save User Info - Firebase Realtime Firebase*/
            validateDate()
        }
    }

    // Input Data

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateDate() {
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        // validate Data
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter your name..!", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email pattern
            Toast.makeText(this, "Invalid Email Address..!", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter password..!", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            Toast.makeText(this, "Confirm password..!", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Password dosen't match", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    // create Account - Firebase Auth

    private fun createUserAccount() {
        progressDialog.setMessage("Creating Account ...")
        progressDialog.show()

        // create user in firebase auth

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // account created , add user info in DB

                updateUserInfo()
            }
            .addOnFailureListener {
                // failed creating account
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account ", Toast.LENGTH_SHORT).show()

            }
    }

    private fun updateUserInfo() {
        //Save User Info - Firebase Realtime Firebase

        progressDialog.setMessage("Saving User Info...")

        // timestamp
        val timestamp = System.currentTimeMillis()

        // get current user ID
        val uid = firebaseAuth.uid

        // setup data to add in DB
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // in profile edit
        hashMap["userType"] = "user" // values (user / admin)
        hashMap["timestamp"] = timestamp

        // set data to DB
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                // user info saved , open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Account created..!", Toast.LENGTH_SHORT).show()
                val i = Intent(this, DashboardUserActivity::class.java)
                startActivity(i)
                finish()

            }
            .addOnFailureListener { e ->
                // failed adding data to DB
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving user info ", Toast.LENGTH_SHORT).show()

            }

    }
}
