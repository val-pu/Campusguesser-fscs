package de.hhufscs.campusguesser.creator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.IGuess
import de.hhufscs.campusguesser.core.LocalGuess
import de.hhufscs.campusguesser.services.repositories.LocalGuessRepository
import de.hhufscs.campusguesser.ui.views.RoundedImageView

class CreatorInstancesRecyclerAdapter(val guesses: List<LocalGuess>) :
    RecyclerView.Adapter<CreatorInstancesRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_creatog_guess, parent, false)
        )
    }

    override fun getItemCount(): Int = guesses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(guesses[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guessRepository = LocalGuessRepository(itemView.context)

        fun bind(guess: IGuess) {
            title.text = "Hier fehlt ein Text"
            guess.getLocation{
                location.text = it.toString()
            }
            guess.getPicture {
                bitmap -> image.drawable = bitmap.toDrawable(itemView.resources)
            }
        }

        val title: TextView = itemView.findViewById(R.id.title)
        val image: RoundedImageView = itemView.findViewById(R.id.image)
        val location: TextView = itemView.findViewById(R.id.app_name)
    }
}