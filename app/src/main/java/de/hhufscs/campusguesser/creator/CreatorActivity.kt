package de.hhufscs.campusguesser.creator

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.services.AssetService

class CreatorActivity : AppCompatActivity() {
    private lateinit var btnCreate: Button
    private lateinit var image: ImageView
    private lateinit var locationText: TextView
    private lateinit var btnDeleteAllAssets: FloatingActionButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator)

        btnDeleteAllAssets = findViewById(R.id.btn_delete_all)
        btnCreate = findViewById(R.id.btn_create)
        locationText = findViewById(R.id.location)


        initGuessInstancesRecycler();



        setUpButtons()
        object : AsyncTask<Void, Void?, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {

                AssetService.getAllSavedJSONFiles(applicationContext).forEach {
                    val fileName = it.replace(".json",".png")
                    val img = AssetService.loadBitmapFromStorage(fileName, applicationContext)
                    AssetService.saveBitmapToInternalStorage(fileName, img!!, applicationContext)
                }
                return null
            }

        }

    }

    private fun initGuessInstancesRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = CreatorInstancesRecyclerAdapter(this)
    }

    private fun setUpButtons() {

        btnDeleteAllAssets.setOnClickListener {
            AssetService.deleteAllSavedFiles(this)
            initGuessInstancesRecycler()
        }

        btnCreate.setOnClickListener { _ ->
            startActivity(Intent(this, CreatorNewInstanceValidatorActivity::class.java))
        }
    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 42
    }


}