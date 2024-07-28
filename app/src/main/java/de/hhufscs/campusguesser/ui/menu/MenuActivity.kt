package de.hhufscs.campusguesser.ui.menu

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.creator.CreatorActivity
import de.hhufscs.campusguesser.ui.GuessActivity

class MenuActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_menu_new)

        findViewById<TextView>(R.id.btn_play_local).setOnClickListener {
            var localIntent: Intent = Intent(applicationContext, GuessActivity::class.java)
            localIntent.putExtra("online", false)
            startActivity(localIntent)
        }

        findViewById<TextView>(R.id.btn_play_online).setOnClickListener {
            var onlineIntent: Intent = Intent(applicationContext, GuessActivity::class.java)
            onlineIntent.putExtra("online", true)
            startActivity(onlineIntent)
        }

        findViewById<TextView>(R.id.btn_editor).setOnClickListener {
            startActivity(Intent(applicationContext, CreatorActivity::class.java))
        }
    }

}