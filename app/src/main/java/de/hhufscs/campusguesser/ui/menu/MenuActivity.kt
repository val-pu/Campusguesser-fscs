package de.hhufscs.campusguesser.ui.menu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.hhufscs.campusguesser.databinding.ActivityMenuBinding
import de.hhufscs.campusguesser.ui.game.GuessActivity

class MenuActivity : AppCompatActivity() {


    lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btnQuickplay.setOnClickListener {
                startOnlineLevel(null)
            }

            btnEsag.setOnClickListener {
                startOnlineLevel(10)
            }

            btnBrowser.setOnClickListener{
                //
            }
        }
    }

    fun startOnlineLevel(guessCount: Int?){
        val onlineIntent = Intent(applicationContext, GuessActivity::class.java)
        onlineIntent.putExtra("count", guessCount ?: 40)
        startActivity(onlineIntent)
    }

}