package com.example.kofy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kofy.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Kofy)  // Define o tema da atividade
        binding = ActivityFavouriteBinding.inflate(layoutInflater)  // Inicializa o view binding
        setContentView(binding.root)  // Define o layout da tela como o vinculado no binding
        enableEdgeToEdge()  // Ativa o edge-to-edge para aproveitar toda a tela do dispositivo

        setContentView(R.layout.activity_favourite)

        // Configura o listener para ajustar o layout conforme os `Insets` das barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}