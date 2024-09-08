package de.hhufscs.campusguesser.creator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.databinding.ActivityCreatorBinding
import de.hhufscs.campusguesser.services.repositories.LocalGuessRepository
import java.util.stream.Collectors

class CreatorActivity : AppCompatActivity() {
    private val guessRepository = LocalGuessRepository(this)
    private lateinit var binding: ActivityCreatorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)



        initGuessInstancesRecycler()
        setUpButtons()
    }

    private fun initGuessInstancesRecycler() {
        val recycler = binding.recycler
        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = CreatorInstancesRecyclerAdapter(guessRepository
            .getAllLocalGuessIDs()
            .stream()
            .map { guessID: String -> LocalGuess(guessID, baseContext) }
            .collect(Collectors.toList()))
    }

    private fun setUpButtons() {
        binding.btnTakePicture.setOnClickListener { _ ->
            startActivity(Intent(this, CreatorNewInstanceValidatorActivity::class.java))
        }
    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 42
    }


}