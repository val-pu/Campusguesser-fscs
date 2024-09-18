package de.hhufscs.campusguesser.ui.menu

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.IGuess
import de.hhufscs.campusguesser.core.Level
import de.hhufscs.campusguesser.core.LevelInformation
import de.hhufscs.campusguesser.ui.game.GuessActivity

class ChooseLevelRecyclerAdapter(val levelInformation: List<LevelInformation>, var applicationContext: Context) :
    RecyclerView.Adapter<ChooseLevelRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_level_overview, parent, false), applicationContext
        )
    }

    override fun getItemCount(): Int = levelInformation.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(levelInformation[position])
    }

    class ViewHolder(itemView: View, var applicationContext: Context) : RecyclerView.ViewHolder(itemView) {

        fun bind(level: LevelInformation) {
            title.text = level.getName()
            countGuesses.text = "${level.getNumberOfGuesses()} Guesses"
            fun startMatchingActivity(){startGuessActivity(level.getID())}
            title.setOnClickListener(){startMatchingActivity()}
        }

        val title: TextView = itemView.findViewById(R.id.level_overview_title)
        val countGuesses: TextView = itemView.findViewById(R.id.level_overview_count)

        fun startGuessActivity(uuid: String){
            var intent: Intent = Intent(applicationContext, GuessActivity::class.java)
            intent.putExtra("online", true)
            intent.putExtra("uuid", uuid)
            applicationContext.startActivity(intent)
        }
    }
}