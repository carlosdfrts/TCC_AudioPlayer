package com.example.kofy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kofy.databinding.ActivityPlaylistBinding

// Activity que gerencia a exibição e interação com playlists de músicas
class PlaylistActivity : AppCompatActivity() {

    // Ligação com o layout da atividade através do ViewBinding (ActivityPlaylistBinding)
    private lateinit var binding: ActivityPlaylistBinding

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define o tema do aplicativo
        setTheme(R.style.Theme_Kofy)

        // Infla o layout usando ViewBinding para conectar a interface gráfica ao código
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Define o conteúdo da atividade

        // Ativa a funcionalidade de borda a borda (Edge-to-Edge) para layout
        enableEdgeToEdge()

        // Sobrescreve o setContentView para garantir que o layout correto seja exibido
        setContentView(R.layout.activity_playlist)

        // Aplica margens para os elementos do layout, levando em consideração as bordas do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())  // Obtém as margens do sistema (barra de status, navegação)

            // Define o preenchimento (padding) com base nas margens do sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets  // Retorna os insets para garantir que o layout seja aplicado corretamente
        }
    }
}