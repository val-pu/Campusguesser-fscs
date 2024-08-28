package de.hhufscs.campusguesser.ui.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.databinding.ActivityLevelOverviewBinding

class ChooseLevelActivity: AppCompatActivity() {
    lateinit var binding: ActivityLevelOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevelOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}