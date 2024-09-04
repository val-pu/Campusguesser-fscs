package de.hhufscs.campusguesser.ui.menu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.hhufscs.campusguesser.creator.CreatorActivity
import de.hhufscs.campusguesser.databinding.ActivityMenuNewBinding
import de.hhufscs.campusguesser.ui.game.GuessActivity

class MenuActivity : AppCompatActivity() {


    lateinit var binding: ActivityMenuNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btnPlayLocal.setOnClickListener {
                val localIntent = Intent(applicationContext, GuessActivity::class.java)
                localIntent.putExtra("online", false)
                startActivity(localIntent)
            }

            btnEditor.setOnClickListener {
                val localIntent = Intent(applicationContext, CreatorActivity::class.java)
                localIntent.putExtra("online", false)
                startActivity(localIntent)
            }

            btnBrowser.setOnClickListener{
                startActivity(Intent(applicationContext, ChooseLevelActivity::class.java))
            }
        }
    }

}