package com.github.travelplannerapp.travels

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem

import com.github.travelplannerapp.R
import com.github.travelplannerapp.addtravel.AddTravelActivity
import com.github.travelplannerapp.traveldetails.TravelDetailsActivity

import javax.inject.Inject

import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_travels.*


class TravelsActivity : AppCompatActivity(), TravelsContract.View {

    @Inject
    lateinit var presenter: TravelsContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travels)

        setSupportActionBar(toolbarTravels)

        fabTravels.setOnClickListener {
            showAddTravel()
        }

        //TODO("[Dorota] check if possible to use dagger2 with adapter")
        recyclerViewTravels.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewTravels.adapter = TravelsAdapter(presenter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun showAddTravel() {
        val intent = Intent(this, AddTravelActivity::class.java)
        startActivity(intent)
    }

    override fun showTravelDetails(travel: String) {
        val intent = Intent(this, TravelDetailsActivity::class.java)
        intent.putExtra(TravelDetailsActivity.EXTRA_TRAVEL_ID, travel)
        startActivity(intent)
    }
}
