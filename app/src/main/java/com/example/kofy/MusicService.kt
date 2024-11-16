package com.example.kofy

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat

// MusicService é uma classe que estende Service e é responsável por controlar a reprodução de música em segundo plano, utilizando o MediaPlayer do Android.
// myBinder: Um objeto da classe interna MyBinder, usado para criar a ligação (bind) entre a Activity e o Service, permitindo interações.
// mediaPlayer: Instância do MediaPlayer, que será usada para gerenciar a reprodução de áudio.
// mediaSession: Instância de MediaSessionCompat, que facilita o controle da mídia e a integração com notificações e outros controles de mídia
// (ex. controle em tela de bloqueio).
class MusicService:Service() {
    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var runnable: Runnable

    // onBind() é chamado quando a Activity se liga ao Service. Ele retorna o IBinder (myBinder) para que a Activity possa se comunicar com o Service.
    // mediaSession: Aqui, uma nova sessão de mídia é criada com o nome "Minha música", que será usada para controlar e monitorar o estado da reprodução de áudio.
    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "Minha música")
        return myBinder
    }

    // MyBinder: Classe interna que estende Binder, usada para fornecer uma instância do MusicService à Activity que se conecta ao serviço.
    // O método currentService() retorna a instância atual de MusicService, permitindo que a Activity acesse as funções e propriedades do serviço.
    inner class MyBinder:Binder() {
        fun currentService():MusicService {
            return this@MusicService
        }
    }
    fun showNotification(playPauseBtn: Int) {

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT )
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if(imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt,0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_splash_screen)
        }

        // Verifica se a música atual é favorita
        val isFavourite = FavouriteActivity.favouriteSongs.any { it.id == PlayerActivity.musicListPA[PlayerActivity.songPosition].id }
        val favouriteIcon = if (isFavourite) R.drawable.favourite_icon else R.drawable.favourite_empty_icon

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            // Adiciona o ícone de favorito na notificação
            .addAction(favouriteIcon, "Favoritar", PendingIntent.getBroadcast(baseContext, 0, Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.FAVOURITE), PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(R.drawable.previous_icon, "Anterior", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_icon, "Próximo", nextPendingIntent)
            .addAction(R.drawable.exit_icon, "Sair", exitPendingIntent)
            .build()

        startForeground(13, notification)
    }
    fun seekBarSetup() {
        runnable = Runnable {
            try {
                mediaPlayer?.let { player ->
                    // Verifica se o MediaPlayer não está nulo e se está em um estado válido
                    if (player.isPlaying) {
                        PlayerActivity.binding.tvSeekBarStart.text = formatDuration(player.currentPosition.toLong())
                        PlayerActivity.binding.seekBarPA.progress = player.currentPosition
                    }
                }
            } catch (e: IllegalStateException) {
                // Log para depuração
                Log.e("MusicService", "Erro ao acessar o estado do MediaPlayer: ${e.message}")
            }
            // Atualiza o runnable após um atraso
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }
    fun createMediaPlayer() {
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()  // Inicializa o MediaPlayer se estiver nulo
            PlayerActivity.musicService!!.mediaPlayer!!.reset()  // Reseta o MediaPlayer para tocar nova música
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)  // Define o caminho da música atual
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()  // Prepara o MediaPlayer
            PlayerActivity.binding.playPausePA.setIconResource(R.drawable.pause_icon)  // Atualiza o ícone para "pausar"
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)  // Mostra a notificação de controle de música
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}