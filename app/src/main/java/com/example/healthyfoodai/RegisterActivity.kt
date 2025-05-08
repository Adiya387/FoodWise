package com.example.healthyfoodai

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthyfoodai.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Для отображения пароля
        binding.checkboxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val inputType = if (isChecked)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.etPassword.inputType = inputType
            binding.etConfirmPassword.inputType = inputType

            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            // Проверка валидности email и пароля
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Введите корректный email"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.etPassword.error = "Пароль должен быть не менее 6 символов"
                return@setOnClickListener
            }
            if (password != confirm) {
                binding.etConfirmPassword.error = "Пароли не совпадают"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()

                    // Переход к UserNameActivity для ввода имени
                    val intent = Intent(this, UserNameActivity::class.java)
                    startActivity(intent)
                    finish()  // Закрыть текущую активность
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка регистрации: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}

