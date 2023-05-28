package com.example.finalproject2.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.finalproject2.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // firebase authentication

    private lateinit var firebaseAuth: FirebaseAuth

    //progress Dialog

    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize firebase authentication

        firebaseAuth = FirebaseAuth.getInstance()

        // initialize progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle No Account click

        binding.noAccountTv.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)

        }

        // handle have Account click

        binding.loginBtn.setOnClickListener {
            /*Steps
            *1-Input Data
            *2-Validate Data
            *3- Login - Firebase Auth
            *4-Check user type - Firebase Auth
            *   *if User - Move to User dashboard
            *   *if Admin - Move to Admin dashboard  */
            validateData()
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        // Input Data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        // Validate Data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show()

        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }


    }

    private fun loginUser() {
        // Login - Firebase Auth

        progressDialog.setMessage("Logging In...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login Success
                checkUser()

            }
            .addOnFailureListener {
                //failed login
                progressDialog.dismiss()
                Toast.makeText(this, "Login Failed ", Toast.LENGTH_SHORT).show()


            }
    }

    private fun
            checkUser() {
        /* Check user type - Firebase Auth
         *   *if User - Move to User dashboard
         *   *if Admin - Move to Admin dashboard  */

        progressDialog.setMessage("Checking User...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    // get user type , user or admin
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
                        //open user dashboard
                        val i = Intent(this@LoginActivity, DashboardUserActivity::class.java)
                        startActivity(i)
                        finish()

                    } else if (userType == "admin") {
                        //open admin dashboard
                        val i = Intent(this@LoginActivity, DashboardAdminActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}