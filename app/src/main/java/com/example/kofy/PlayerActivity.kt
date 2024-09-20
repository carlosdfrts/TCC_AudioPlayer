package com.example.kofy

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kofy.databinding.ActivityPlayerBinding

// PlayerActivity controla a reprodução de músicas e gerencia a comunicação com o MusicService
class PlayerActivity : AppCompatActivity(), ServiceConnection {

    // Objetos estáticos (companion object) usados para compartilhar a lista de músicas e estado de reprodução
    companion object {
        lateinit var musicListPA: ArrayList<Music>  // Lista de músicas carregadas
        var songPosition: Int = 0  // Posição atual da música
        var isPlaying: Boolean = false  // Estado de reprodução (tocando ou pausado)
        var musicService: MusicService? = null  // Referência ao serviço de música
    }

    private lateinit var binding: ActivityPlayerBinding  // Ligação para a interface (ActivityPlayerBinding)

    // Método chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Kofy)  // Define o tema do aplicativo
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Iniciar o serviço de música e conectá-lo
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        // Inicializar layout com base na música selecionada
        initializeLayout()

        // Botão de play/pause - alterna entre tocar e pausar a música
        binding.playPausePA.setOnClickListener {
            if (isPlaying) pauseMusic() else playMusic()
        }

        // Botão de música anterior
        binding.previousBtnPA.setOnClickListener {
            prevNextSong(increment = false)  // Reproduz a música anterior
        }

        // Botão de próxima música
        binding.nextBtnPA.setOnClickListener {
            prevNextSong(increment = true)  // Reproduz a próxima música
        }

        // Configuração de margens do layout com base nas bordas do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Método chamado quando a atividade é destruída (libera recursos)
    override fun onDestroy() {
        super.onDestroy()
        musicService!!.mediaPlayer?.release()  // Libera o MediaPlayer
        musicService!!.mediaPlayer = null  // Limpa a referência ao MediaPlayer
    }

    // Inicializa o layout da atividade com base na música atual
    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)  // Obtém a posição da música a partir da intenção
        when (intent.getStringExtra("class")) {
            "MusicAdapter" -> {  // Se veio da classe MusicAdapter
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)  // Carrega todas as músicas da lista principal
                setLayout()  // Configura o layout com a música selecionada
            }
            "MainActivity" -> {  // Se veio da classe MainActivity
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()  // Embaralha a lista de músicas
                setLayout()
            }
        }
    }

    // Configura a interface do player com a música atual
    private fun setLayout() {
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)  // Carrega a imagem do álbum da música atual
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash_screen).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title  // Define o título da música atual
    }

    // Cria e inicializa o MediaPlayer com a música atual
    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()  // Inicializa o MediaPlayer se estiver nulo
            musicService!!.mediaPlayer!!.reset()  // Reseta o MediaPlayer para tocar nova música
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)  // Define o caminho da música atual
            musicService!!.mediaPlayer!!.prepare()  // Prepara o MediaPlayer
            musicService!!.mediaPlayer!!.start()  // Inicia a reprodução
            isPlaying = true
            binding.playPausePA.setIconResource(R.drawable.pause_icon)  // Atualiza o ícone para "pausar"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Toca a música atual
    private fun playMusic() {
        musicService!!.mediaPlayer?.let {
            it.start()  // Inicia a reprodução
            binding.playPausePA.setIconResource(R.drawable.pause_icon)  // Atualiza o ícone para "pausar"
            isPlaying = true
        }
    }

    // Pausa a música atual
    private fun pauseMusic() {
        musicService!!.mediaPlayer?.let {
            it.pause()  // Pausa a reprodução
            binding.playPausePA.setIconResource(R.drawable.play_icon)  // Atualiza o ícone para "play"
            isPlaying = false
        }
    }

    // Reproduz a próxima ou a música anterior
    private fun prevNextSong(increment: Boolean) {
        setSongPosition(increment)  // Atualiza a posição da música
        setLayout()  // Atualiza o layout com a nova música
        createMediaPlayer()  // Cria o MediaPlayer para a nova música
    }

    // Define a posição da próxima música ou da anterior
    private fun setSongPosition(increment: Boolean) {
        songPosition = if (increment) {
            (songPosition + 1) % musicListPA.size  // Próxima música
        } else {
            if (songPosition - 1 < 0) musicListPA.size - 1 else songPosition - 1  // Música anterior
        }
    }

    // Método chamado quando o serviço é conectado
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()  // Obtém a instância do serviço de música
        createMediaPlayer()  // Cria o MediaPlayer para a música atual
        musicService!!.showNotification()  // Mostra a notificação de controle de música
    }

    // Método chamado quando o serviço é desconectado
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null  // Remove a referência ao serviço
    }
}
