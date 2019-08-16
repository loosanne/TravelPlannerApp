package com.github.travelplannerapp.travels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.RecyclerView

import com.github.travelplannerapp.R
import com.github.travelplannerapp.addtravel.AddTravelActivity
import com.github.travelplannerapp.communication.ServerApi
import com.github.travelplannerapp.traveldetails.TravelDetailsActivity
import com.github.travelplannerapp.utils.DrawerUtils

import javax.inject.Inject

import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_travels.*

class TravelsActivity : AppCompatActivity(), TravelsContract.View {

    @Inject
    lateinit var presenter: TravelsContract.Presenter
    private var myCompositeDisposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travels)
        myCompositeDisposable = CompositeDisposable()

        // Set up toolbar
        setSupportActionBar(toolbarTravels)
        supportActionBar?.setHomeButtonEnabled(true)
        DrawerUtils.getDrawer(this, toolbarTravels)

        fabTravels.setOnClickListener {
            showAddTravel()
        }

        recyclerViewTravels.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewTravels.adapter = TravelsAdapter(presenter)
    }

    override fun onResume() {
        super.onResume()
        presenter.loadTravels()
    }

    override fun onDestroy() {
        super.onDestroy()
        myCompositeDisposable?.clear()
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

    override fun showNoTravels() {
        textViewNoTravels.visibility = View.VISIBLE
        recyclerViewTravels.visibility = View.GONE
    }

    override fun showTravels() {
        textViewNoTravels.visibility = View.GONE
        recyclerViewTravels.visibility = View.VISIBLE
    }

    override fun showSnackbar(message: String) {
        Snackbar.make(coordinatorLayoutTravels, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }

    override fun loadTravels(requestInterface: ServerApi, handleResponse: (myTravels: List<String>) -> Unit) {
        val sharedPref = getSharedPreferences(resources.getString(R.string.auth_settings),
                Context.MODE_PRIVATE)
        val email = sharedPref.getString(resources.getString(R.string.email_shared_pref),
                "default").toString()
        val authToken = sharedPref.getString(resources.getString(R.string.auth_token_shared_pref),
                "default").toString()

        myCompositeDisposable?.add(requestInterface.getTravels(email, authToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(handleResponse, { showSnackbar(resources.getString(R.string.server_connection_failure)) }))
    }
}
