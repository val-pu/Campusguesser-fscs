package de.hhufscs.campusguesser.ui.menu

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.creator.CreatorActivity
import de.hhufscs.campusguesser.services.AssetService
import de.hhufscs.campusguesser.ui.GuessActivity

class MenuActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TextView>(R.id.btn_play).post {
            AssetService.legacyPfusch(this)
        }

        setContentView(R.layout.activity_menu)

        findViewById<TextView>(R.id.btn_play).setOnClickListener {
            startActivity(Intent(applicationContext, GuessActivity::class.java))
        }

        findViewById<TextView>(R.id.btn_editor).setOnClickListener {
            startActivity(Intent(applicationContext, CreatorActivity::class.java))
        }
    }

}