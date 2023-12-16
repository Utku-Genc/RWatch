package com.example.kotlindeneme4

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindeneme4.databinding.ActivitySignupBinding
import com.example.kotlindeneme4.model.UserModel
import com.example.kotlindeneme4.util.UiUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitBtn.setOnClickListener{
            signup()
        }

        binding.goToLoginBtn.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
    }

    fun setInProgress(inProgress : Boolean){
        if (inProgress){
            binding.progressBar.visibility = View.VISIBLE
            binding.submitBtn.visibility = View.GONE
        }
        else{
            binding.progressBar.visibility = View.GONE
            binding.submitBtn.visibility = View.VISIBLE
        }
    }

    fun signup(){
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.setError("E-posta geçerli değil.")
            return
        }

        if (password.length<6){
            binding.passwordInput.setError("Şifre en az 6 karakter  olmalıdır.")
            return
        }

        if (password!=confirmPassword){
            binding.confirmPasswordInput.setError("Şifreler eşleşmiyor.")
            return
        }

        signupWithFirebase(email,password)

    }
    fun signupWithFirebase(email: String, password: String){
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            email,password
        ).addOnSuccessListener {
            it.user?.let {user ->
                val userModel = UserModel( user.uid, email, email.substringBefore("@") )
                Firebase.firestore.collection("users")
                    .document(user.uid)
                    .set(userModel).addOnSuccessListener {
                        UiUtil.showToast(applicationContext,"Hesap başarıyla oluşturuldu.")
                        setInProgress(false)
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    }
            }
        }.addOnFailureListener{
            UiUtil.showToast(applicationContext, it.localizedMessage?: "Bir şeyler ters gitti.")
            setInProgress(false)
        }
    }
}