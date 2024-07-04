package de.hhufscs.campusguesser.creator

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.services.AssetService
import de.hhufscs.campusguesser.services.GuessRepository

class CreatorInstancesRecyclerAdapter(context: Context) :
    RecyclerView.Adapter<CreatorInstancesRecyclerAdapter.ViewHolder>() {

    val fileNames = AssetService.getAllSavedJSONFiles(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.creator_guess_instance_item, parent, false)
        )
    }

    override fun getItemCount(): Int = fileNames.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fileNames[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guessRepository = GuessRepository(itemView.context)

        fun bind(name: String) {
            title.text = name
            location.text = AssetService.readJSONObjectFromFile(name, itemView.context).toString()
            image.setImageBitmap(AssetService.loadBitmapFromStorage(name.replace(".json",".png"), itemView.context))

        }

        val title: TextView = itemView.findViewById(R.id.title)
        val image: ImageView = itemView.findViewById(R.id.image)
        val location: TextView = itemView.findViewById(R.id.location)
    }
}