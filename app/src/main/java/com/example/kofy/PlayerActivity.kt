package com.example.kofy

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kofy.databinding.ActivityPlayerBinding

// PlayerActivity controla a reprodução de músicas e gerencia a comunicação com o MusicService
class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    // Objetos estáticos (companion object) usados para compartilhar a lista de músicas e estado de reprodução
    companion object {
        var musicListPA: ArrayList<Music> = ArrayList()  // Inicializado com uma lista vazia
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var isFavourite: Boolean = false
        var fIndex: Int = -1
    }

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

        binding.backBtnPA.setOnClickListener {
            finish()
        }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Compartilhando Arquivo MP3"))
        }

        binding.favouriteBtnPA.setOnClickListener {
            if (isFavourite) {
                // Se já é favorito, remove
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)

                // Verifique se a música está na lista de favoritos
                if (fIndex != -1 && fIndex < FavouriteActivity.favouriteSongs.size) {
                    FavouriteActivity.favouriteSongs.removeAt(fIndex)
                }
            } else {
                // Se não é favorito, adiciona
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])

                // Atualiza o índice da música favorita
                fIndex = FavouriteActivity.favouriteSongs.indexOf(musicListPA[songPosition])
            }
        }

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

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.repeatBtnPA.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_blue))
            } else {
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.black))
            }
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
            "FavouriteAdapter" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }
            "MusicAdapterSearch" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
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
            "FavouriteShuffle" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                musicListPA.shuffle()  // Embaralha a lista de músicas
                setLayout()
            }
        }
    }

    // Configura a interface do player com a música atual
    private fun setLayout() {
        fIndex = favouriteChecker(musicListPA[songPosition].id) // Verifica se a música atual é favorita
        isFavourite = fIndex != -1 // Atualiza isFavourite com base no índice
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash_screen).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title

        // Atualiza o ícone de favorito de acordo com o estado da música atual
        if (isFavourite) {
            binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
        } else {
            binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
        }

        if(repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_blue))
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
            musicService!!.showNotification(R.drawable.pause_icon)  // Mostra a notificação de controle de música
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Toca a música atual
    private fun playMusic() {
            binding.playPausePA.setIconResource(R.drawable.pause_icon)  // Atualiza o ícone para "pausar"
            musicService!!.showNotification(R.drawable.pause_icon)
            isPlaying = true
            musicService!!.mediaPlayer!!.start()
    }
    // Pausa a música atual
    private fun pauseMusic() {
            binding.playPausePA.setIconResource(R.drawable.play_icon)  // Atualiza o ícone para "play"
            musicService!!.showNotification(R.drawable.play_icon)
            isPlaying = false
            musicService!!.mediaPlayer!!.pause()
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
        musicService!!.showNotification(R.drawable.pause_icon)
        musicService!!.seekBarSetup()
    }

    // Método chamado quando o serviço é desconectado
    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null  // Remove a referência ao serviço
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (repeat) {
            createMediaPlayer() // Recria o MediaPlayer para repetir a música atual
            playMusic() // Começa a tocar novamente
        } else {
            setSongPosition(increment = true) // Passa para a próxima música se não estiver no modo de repetição
            createMediaPlayer() // Cria o MediaPlayer para a nova música
            setLayout() // Atualiza o layout
        }
    }
}
