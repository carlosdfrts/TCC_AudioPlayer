package com.example.kofy

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
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
    fun showNotification() {
        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)

            // setContentTitle() e setContentText(): Definem o título (nome da música) e o texto (nome do artista) da notificação,
            // retirando as informações da lista de músicas da PlayerActivity.
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)

            // setSmallIcon() e setLargeIcon(): Define os ícones para a notificação.
            // O ícone pequeno é mostrado na barra de status, e o ícone grande (arte do álbum) é mostrado na notificação.
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.music_player_icon_splash_screen))

            // setStyle(): Aplica o estilo de mídia à notificação, permitindo mostrar controles de mídia (anterior, play/pause, próximo).
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))

            // setPriority(): Define a prioridade da notificação como PRIORITY_HIGH, para que ela seja exibida de forma proeminente.
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            // setVisibility(): Define a visibilidade da notificação como PUBLIC, o que significa que todos os detalhes da notificação serão visíveis na tela de bloqueio.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // setOnlyAlertOnce(true): Garante que a notificação só alerta uma vez, sem sons repetidos.
            .setOnlyAlertOnce(true)

            // addAction(): Adiciona botões para as ações de "Anterior", "Play/Pause", "Próximo", e "Sair".
            .addAction(R.drawable.previous_icon, "Anterior", null)
            .addAction(R.drawable.play_icon, "Play", null)
            .addAction(R.drawable.next_icon, "Próximo", null)
            .addAction(R.drawable.exit_icon, "Sair", null)
            .build()

        // startForeground(): Inicia o serviço em primeiro plano, com a notificação exibida continuamente enquanto a música estiver tocando.
        // O número 13 é o ID da notificação.
        startForeground(13, notification)
    }
}