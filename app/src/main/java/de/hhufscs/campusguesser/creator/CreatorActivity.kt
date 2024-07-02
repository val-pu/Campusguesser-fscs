package de.hhufscs.campusguesser.creator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hhufscs.campusguesser.R
import de.hhufscs.campusguesser.core.AssetService
import org.json.JSONObject
import java.util.UUID

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