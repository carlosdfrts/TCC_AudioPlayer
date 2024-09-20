package com.example.kofy

import java.util.concurrent.TimeUnit

// Encapsula os dados essenciais de uma música
data class Music(
    val id:String,
    val title:String,
    val album:String,
    val artist:String,
    val path:String,
    val duration: Long = 0,
    val artUri:String) {
}

// Essa função converte a duração da música de milissegundos para um formato mais legível
fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
    return String.format("%2d:%2d", minutes, seconds)
}