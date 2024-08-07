package de.hhufscs.campusguesser.creator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.services.repositories.LocalGuessRepository
import java.util.stream.Collectors

class CreatorActivity : AppCompatActivity() {
    private lateinit var btnCreate: Button
    private lateinit var image: ImageView
    private lateinit var locationText: TextView
    private val guessRepository = LocalGuessRepository(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator)

        btnCreate = findViewById(R.id.btn_create)
        locationText = findViewById(R.id.app_name)


        initGuessInstancesRecycler();
        setUpButtons()
    }

    private fun initGuessInstancesRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = CreatorInstancesRecyclerAdapter(guessRepository
            .getAllLocalGuessIDs()
            .stream()
            .map{guessID: String -> LocalGuess(guessID, this) }
            .collect(Collectors.toList()))
    }

    private fun setUpButtons() {
        btnCreate.setOnClickListener { _ ->
            startActivity(Intent(this, CreatorNewInstanceValidatorActivity::class.java))
        }
    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 42
    }


}