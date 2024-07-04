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
import de.hhufscs.campusguesser.services.GuessRepository

class CreatorActivity : AppCompatActivity() {
    private lateinit var btnCreate: Button
    private lateinit var image: ImageView
    private lateinit var locationText: TextView
    private val guessRepository = GuessRepository(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator)

        btnCreate = findViewById(R.id.btn_create)
        locationText = findViewById(R.id.location)


        initGuessInstancesRecycler();
        setUpButtons()
    }

    private fun initGuessInstancesRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = CreatorInstancesRecyclerAdapter(guessRepository.getAllGuesses())
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