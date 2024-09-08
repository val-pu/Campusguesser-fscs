package de.hhufscs.campusguesser.ui.game.endscreen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.hhufscs.campusguesser.databinding.ActivityEndScreenBinding

class EndScreenActivity : AppCompatActivity() {

    lateinit var binding: ActivityEndScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val levelResult: LevelResultDTO =
            GsonFactory.gsonForProperUseWithIGeoPoint()
                .fromJson(intent.getStringExtra("result")!!, LevelResultDTO::class.java)
        binding = ActivityEndScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.summaryRecycler.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            val guesses = levelResult.results
            adapter = EndScreenSummaryAdapter(guesses)
        }
        binding.endScore.text = "${levelResult.points()}/${levelResult.maxPoints()}"
    }
}