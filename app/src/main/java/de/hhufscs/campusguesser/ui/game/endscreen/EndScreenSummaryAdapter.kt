package de.hhufscs.campusguesser.ui.game.endscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.GuessResult
import de.hhufscs.campusguesser.databinding.ItemSummaryBinding


class EndScreenSummaryAdapter(private val results: List<GuessResult>) :
    Adapter<EndScreenSummaryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemSummaryBinding = ItemSummaryBinding.bind(itemView)

        fun bind(guessResult: GuessResult) {
            binding.progress.apply {
                progress = guessResult.points.toFloat()
                labelText = guessResult.points.toString()
            }

            binding.distanceText.text = itemView
                .resources
                .getString(R.string.summary_daneben)
                .format(guessResult.getDistance())

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_summary, parent, false)
        )

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.bind(result)
    }

}