package com.example.kofy

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return  // Adiciona verificação para evitar NullPointerException

        when (intent.action) {
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context)
            ApplicationClass.PLAY -> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextSong(increment = true, context)
            ApplicationClass.EXIT -> {
                // Adiciona verificação se musicService é nulo
                if (PlayerActivity.musicService != null) {
                    PlayerActivity.musicService!!.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                    PlayerActivity.musicService!!.mediaPlayer?.release()
                    PlayerActivity.musicService = null
                }
                exitProcess(0)  // Use 0 ao invés de 1 para uma saída normal
            }
        }
    }

    private fun playMusic() {
        // Verifica se musicService e mediaPlayer não são nulos
        PlayerActivity.musicService?.let { service ->
            service.mediaPlayer?.start()
            PlayerActivity.isPlaying = true
            service.showNotification(R.drawable.pause_icon)
            PlayerActivity.binding.playPausePA.setIconResource(R.drawable.pause_icon)
        }
    }

    private fun pauseMusic() {
        // Verifica se musicService e mediaPlayer não são nulos
        PlayerActivity.musicService?.let { service ->
            service.mediaPlayer?.pause()
            PlayerActivity.isPlaying = false
            service.showNotification(R.drawable.play_icon)
            PlayerActivity.binding.playPausePA.setIconResource(R.drawable.play_icon)
        }
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment)
        PlayerActivity.musicService?.createMediaPlayer()
        // Verifica se a lista de músicas e a posição da música são válidas
        if (PlayerActivity.songPosition < PlayerActivity.musicListPA.size) {
            Glide.with(context)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(
                    RequestOptions().placeholder(R.drawable.music_player_icon_splash_screen)
                        .centerCrop()
                )
                .into(PlayerActivity.binding.songImgPA)
            PlayerActivity.binding.songNamePA.text =
                PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            playMusic()
            PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
            if (PlayerActivity.isPlaying) PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
            else PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
        }
    }
}