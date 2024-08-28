package de.hhufscs.campusguesser.ui.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.databinding.ActivityLevelOverviewBinding
import de.hhufscs.campusguesser.services.repositories.LevelRepository
import java.util.logging.Level

class ChooseLevelActivity: AppCompatActivity() {
    lateinit var binding: ActivityLevelOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevelOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var repository: LevelRepository = LevelRepository()
        repository.getAllLevelInformations {
            binding.apply{
                levelOverviewRecycler.layoutManager = LinearLayoutManager(applicationContext)
                levelOverviewRecycler.adapter = ChooseLevelRecyclerAdapter(it)
            }
        }
    }
}