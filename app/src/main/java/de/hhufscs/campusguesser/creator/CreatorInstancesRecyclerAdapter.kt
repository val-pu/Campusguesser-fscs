package de.hhufscs.campusguesser.creator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.services.GuessRepository

class CreatorInstancesRecyclerAdapter(val guesses: List<LocalGuess>) :
    RecyclerView.Adapter<CreatorInstancesRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.creator_guess_instance_item, parent, false)
        )
    }

    override fun getItemCount(): Int = guesses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(guesses[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guessRepository = GuessRepository(itemView.context)

        fun bind(guess: LocalGuess) {
            title.text = guess.guessID

            location.text = guess.geoPoint.toString()

            guessRepository.getPictureForGuess(guess, image::setImageBitmap)
        }

        val title: TextView = itemView.findViewById(R.id.title)
        val image: ImageView = itemView.findViewById(R.id.image)
        val location: TextView = itemView.findViewById(R.id.location)
    }
}