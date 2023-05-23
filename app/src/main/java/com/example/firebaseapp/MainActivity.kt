package com.example.firebaseapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.firebaseapp.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class MainActivity : AppCompatActivity() {

  private lateinit var auth: FirebaseAuth
  private lateinit var binding: ActivityMainBinding
  private var storedVerificationId: String? = ""
  private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
  private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    binding.btnGetOtp.setOnClickListener {
      Log.d("firebase-check", binding.edtNumber.text.toString())
      startPhoneNumberVerification(binding.edtNumber.text.toString())
    }

    binding.btnVerifyOtp.setOnClickListener {
      Log.d("firebase-check", binding.edtOtp.text.toString())
      verifyPhoneNumberWithCode(storedVerificationId, binding.edtOtp.text.toString())
    }

    auth = Firebase.auth

    callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
      override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        Log.d("firebase-check", credential.toString())
        signInWithPhoneAuthCredentials(credential)
      }

      override fun onVerificationFailed(error: FirebaseException) {
        Log.d("firebase-check", error.toString())
      }

      override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
      ) {
        storedVerificationId = verificationId
        resendToken = token
      }

    }

  }

  private fun signInWithPhoneAuthCredentials(credential: PhoneAuthCredential) {
    auth.signInWithCredential(credential)
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          Log.d("firebase-check", "signInWithCredential: Successful")
          val user = task.result?.user
          Toast.makeText(this, "Welcome $user", Toast.LENGTH_SHORT).show()
        } else {
          Log.d("firebase-check", "signInWithCredential: Failed")
        }
      }
  }

  private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
    val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
    signInWithPhoneAuthCredentials(credential)
  }

  private fun startPhoneNumberVerification(phoneNumber: String) {
    val options = PhoneAuthOptions.newBuilder(auth)
      .setPhoneNumber(phoneNumber)
      .setTimeout(60L, TimeUnit.SECONDS)
      .setActivity(this)
      .setCallbacks(callbacks)
      .build()
    PhoneAuthProvider.verifyPhoneNumber(options)
  }

  override fun onStart() {
    super.onStart()
    val currentUser = auth.currentUser
    updateUI(currentUser)
  }

  private fun updateUI(currentUser: FirebaseUser?) {

  }
}