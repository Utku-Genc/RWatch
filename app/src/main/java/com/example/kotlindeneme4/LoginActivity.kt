package com.example.kotlindeneme4

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindeneme4.databinding.ActivityLoginBinding
import com.example.kotlindeneme4.util.UiUtil
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().currentUser?.let {
            // Kullanıcı oturum açmışsa MainActivity'e yönlendir
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        auth = FirebaseAuth.getInstance()
        binding.forgotPasswordButton.setOnClickListener {
            showForgotPasswordDialog()
        }


        binding.submitBtn.setOnClickListener {
            login()
        }

        binding.goToSignupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)


        // Özelleştirilmiş bir görünüm (custom view) ekleyin
        val customLayout = layoutInflater.inflate(R.layout.forgot_password_dialog, null)
        builder.setView(customLayout)

        val input = customLayout.findViewById<EditText>(R.id.emailEditText)

        builder.setPositiveButton("Gönder") { dialog, _ ->
            val email = input.text.toString().trim()

            if (email.isEmpty()) {
                input.error = "E-posta boş bırakılamaz."
                return@setPositiveButton
            }

            sendPasswordResetEmail(email)
        }

        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }


    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Şifre sıfırlama e-postası başarıyla gönderildi
                    // Kullanıcıyı bilgilendirin
                    UiUtil.showToast(
                        this,
                        "Şifre sıfırlama e-postası gönderildi. E-posta kutunuzu kontrol edin."
                    )
                } else {
                    // Şifre sıfırlama e-postası gönderilemedi
                    // Kullanıcıyı bilgilendirin
                    UiUtil.showToast(
                        this,
                        "Şifre sıfırlama e-postası gönderilemedi. Lütfen tekrar deneyin."
                    )

                }
            }
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.submitBtn.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.submitBtn.visibility = View.VISIBLE
        }
    }

    fun login() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError("E-posta geçerli değil.")
            return
        }

        if (password.length < 6) {
            binding.passwordInput.setError("Şifre en az 6 karakter  olmalıdır.")
            return
        }
        signupWithFirebase(email, password)
    }

    fun signupWithFirebase(email: String, password: String) {
        setInProgress(true)

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            email,
            password
        ).addOnSuccessListener { authResult ->
            val currentUser = authResult.user

            // Check if the user is blocked
            if (currentUser != null) {
                // Fetch the user data from Firestore
                Firebase.firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val isBlocked = document.getBoolean("block") ?: false

                        if (isBlocked) {
                            // User is blocked, show a message and do not proceed
                            UiUtil.showToast(this, "Hesabınız engellendi. Giriş yapılamıyor.")
                            setInProgress(false)
                        } else {
                            // User is not blocked, proceed to MainActivity
                            UiUtil.showToast(this, "Giriş başarılı.")
                            setInProgress(false)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        // Error while fetching user data, show a generic error message
                        UiUtil.showToast(this, "Bir şeyler ters gitti.")
                        setInProgress(false)
                    }
            }
        }.addOnFailureListener {
            UiUtil.showToast(applicationContext, it.localizedMessage ?: "Bir şeyler ters gitti.")
            setInProgress(false)
        }
    }
}