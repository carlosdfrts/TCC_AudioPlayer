package com.example.kofy

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kofy.databinding.ActivityMainBinding
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding  // Usa o view binding para acessar as views da activity.
    private lateinit var toggle: ActionBarDrawerToggle  // Controla o menu lateral (drawer).
    private lateinit var musicAdapter: MusicAdapter  // Adaptador para exibir a lista de músicas.

    companion object {
        lateinit var MusicListMA: ArrayList<Music>  // Lista de músicas que será preenchida.
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLayout()

        // Configura a UI para ser adaptável a diferentes layouts de tela com o edge-to-edge
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura o botão de aleatorização
        binding.shuffleBtn.setOnClickListener {
            if (MusicListMA.isNotEmpty()) {
                val randomIndex = (0 until MusicListMA.size).random()
                val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                intent.putExtra("index", randomIndex)
                intent.putExtra("class", "MainActivity")
                startActivity(intent)  // Inicia o player de música com uma música aleatória
            } else {
                Toast.makeText(this, "Nenhuma música disponível", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão de favoritos
        binding.favouriteBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlayerActivity::class.java))  // Navega para o player
        }

        // Botão de playlists
        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))  // Navega para a tela de playlists
        }

        // Configura o menu lateral
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navFeedback -> Toast.makeText(baseContext, "Feedback", Toast.LENGTH_SHORT).show()
                R.id.navSettings -> Toast.makeText(baseContext, "Configurações", Toast.LENGTH_SHORT).show()
                R.id.navAbout -> Toast.makeText(baseContext, "Sobre", Toast.LENGTH_SHORT).show()
                R.id.navExit -> exitProcess(1)  // Sai do aplicativo
            }
            true
        }

        // Solicita permissão para acessar o armazenamento
        requestRuntimePermissions()
    }

    private fun requestRuntimePermissions() {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        // Se há permissões pendentes, solicita-as
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 13)
        } else {
            loadMusicData()  // Caso já tenha as permissões, carrega a lista de músicas
        }
    }

    // Trata o resultado da solicitação de permissões
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão Concedida", Toast.LENGTH_SHORT).show()
                loadMusicData()
            } else {
                Toast.makeText(this, "Permissão Negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_Kofy)
        setContentView(binding.root)

        // Configuração do Toolbar
        setSupportActionBar(binding.toolbar)

        // Configuração do DrawerLayout e ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(this, binding.root, binding.toolbar, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()

        // Habilitar o botão de navegação
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.hamburguer_icon)

        // Configuração do RecyclerView
        // O carregamento da lista de músicas será feito após a concessão de permissões
    }

    @SuppressLint("Recycle", "Range")
    private fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"  // Seleciona apenas arquivos de música
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Faz a query para recuperar as músicas do armazenamento
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            null
        )

        // Itera pelos resultados e preenche a lista de músicas
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, duration = durationC, path = pathC,
                        artUri = artUriC)
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
            cursor.close()
        }
        return tempList  // Retorna a lista de músicas encontradas
    }
    // Carrega e exibe as músicas no RecyclerView
    private fun loadMusicData() {
        MusicListMA = getAllAudio()
        binding.songsRecyclerView.setHasFixedSize(true)
        binding.songsRecyclerView.setItemViewCacheSize(13)
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.songsRecyclerView.adapter = musicAdapter
        binding.totalSongsTextView.text = "Músicas Totais: ${musicAdapter.itemCount}"
    }
}