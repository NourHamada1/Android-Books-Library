package com.example.finalproject2.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject2.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUser()
        }, 1000)
    }

    private fun checkUser() {
        // get current user if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        } else {
            // user logged in
            val firebaseUser = firebaseAuth.currentUser!!

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        // get user type , user or admin
                        val userType = snapshot.child("userType").value
                        if (userType == "user") {
                            //open user dashboard
                            val i = Intent(this@SplashActivity, DashboardUserActivity::class.java)
                            startActivity(i)
                            finish()

                        } else if (userType == "admin") {
                            //open admin dashboard
                            val i = Intent(this@SplashActivity, DashboardAdminActivity::class.java)
                            startActivity(i)
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}