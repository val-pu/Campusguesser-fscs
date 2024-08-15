package de.hhufscs.campusguesser.ui.game.endscreen

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.GuessResult
import de.hhufscs.campusguesser.databinding.ActivityEndScreenBinding
import de.hhufscs.campusguesser.ui.game.GEOPOINT_HHU
import java.util.LinkedList

class EndScreenActivity : AppCompatActivity() {

    lateinit var binding: ActivityEndScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEndScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.summaryRecycler.apply {
            layoutManager = LinearLayoutManager(applicationContext)

            val guesses = LinkedList<GuessResult>()

            guesses.add(GuessResult(GEOPOINT_HHU, GEOPOINT_HHU,(100*Math.random()).toInt()))
            guesses.add(GuessResult(GEOPOINT_HHU, GEOPOINT_HHU,(100*Math.random()).toInt()))
            guesses.add(GuessResult(GEOPOINT_HHU, GEOPOINT_HHU,(100*Math.random()).toInt()))
            guesses.add(GuessResult(GEOPOINT_HHU, GEOPOINT_HHU,(100*Math.random()).toInt()))
            adapter = EndScreenSummaryAdapter(guesses)
        }
    }
}