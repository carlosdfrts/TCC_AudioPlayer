package com.example.kofy

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.kofy.databinding.MusicViewBinding

// MusicAdapter é um adaptador personalizado que estende RecyclerView.
// Adapter e utiliza um ViewHolder interno chamado MyHolder.
// Ele é responsável por exibir a lista de músicas em uma RecyclerView.
class MusicAdapter(private val context: Context, private val musicList: ArrayList<Music>) : RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    // MyHolder é a classe interna que armazena as referências às Views necessárias no layout de cada item da lista.
    // title: Referência ao TextView que exibe o nome da música.
    // album: Referência ao TextView que exibe o nome do álbum.
    // image: Referência ao ImageView que exibe a imagem do álbum (arte do álbum).
    // duration: Referência ao TextView que exibe a duração da música.
    // root: Referência ao layout raiz do item de música, que será usado para adicionar eventos de clique.
    class MyHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    // Este método vincula os dados da lista de músicas (musicList) às views do ViewHolder.
    // holder.title.text: Define o título da música.
    // holder.album.text: Define o álbum da música.
    // holder.duration.text: Formata a duração da música e a define no TextView correspondente, usando a função formatDuration().
    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)

        // A biblioteca Glide é usada para carregar a arte do álbum a partir de um URI.
        // Usa o método load() para buscar o artUri.
        // Aplica uma RequestOptions para definir um placeholder (music_player_icon_splash_screen)
        // que é exibido enquanto a imagem é carregada e também faz um corte central na imagem (centerCrop()).
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_splash_screen).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "MusicAdapter")
            ContextCompat.startActivity(context, intent, null)
        }
    }

    // Retorna o número total de itens na lista, ou seja, o tamanho de musicList.
    override fun getItemCount(): Int {
        return musicList.size
    }
}
