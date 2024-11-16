package com.example.kofy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kofy.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    companion object {
        var favouriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Kofy)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurações do RecyclerView
        binding.favouriteRV.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = GridLayoutManager(this@FavouriteActivity, 4)
            adapter = FavouriteAdapter(this@FavouriteActivity, favouriteSongs)
        }

        // Exibe ou oculta o botão de shuffle baseado na lista de músicas favoritas
        if (favouriteSongs.isEmpty()) {
            binding.shuffleBtnFA.visibility = View.INVISIBLE
        } else {
            binding.shuffleBtnFA.visibility = View.VISIBLE
        }

        binding.shuffleBtnFA.setOnClickListener {
            if (favouriteSongs.isNotEmpty()) {  // Garante que há músicas favoritas
                val randomIndex = (0 until favouriteSongs.size).random()  // Escolhe um índice aleatório da lista de favoritas
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("index", randomIndex)  // Passa o índice da música escolhida
                intent.putExtra("class", "FavouriteShuffle")  // Indica que é uma música aleatória da lista de favoritos
                startActivity(intent)  // Inicia o player de música com uma música aleatória
            } else {
                Toast.makeText(this, "Nenhuma música favorita disponível", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backBtnFA.setOnClickListener { finish() }
        // Ajusta o layout conforme os insets das barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
