package com.github.travelplannerapp.traveldetails.searchelement

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.github.travelplannerapp.R
import com.github.travelplannerapp.communication.appmodel.CityObject
import com.github.travelplannerapp.communication.commonmodel.Contacts
import com.github.travelplannerapp.communication.commonmodel.Place
import com.github.travelplannerapp.utils.ActivityUtils
import com.google.android.material.snackbar.Snackbar
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.common.ViewObject
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_search_element.*
import kotlinx.android.synthetic.main.fab_check.*
import kotlinx.android.synthetic.main.item_place_element_info.*
import javax.inject.Inject


class SearchElementActivity : AppCompatActivity(), SearchElementContract.View {

    @Inject
    lateinit var presenter: SearchElementContract.Presenter
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var map: Map
    private val placesContainer = MapContainer()
    private lateinit var selectedMapMarker: MapMarker

    companion object {
        const val EXTRA_PLACE = "place"
        const val EXTRA_CATEGORY = "category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_element)
        initializeMap()

        fabCheck.setOnClickListener {
            returnResultAndFinish()
        }

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchViewCity.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)

            setOnSuggestionListener(object : androidx.appcompat.widget.SearchView.OnSuggestionListener {
                override fun onSuggestionClick(position: Int): Boolean {
                    ActivityUtils.hideKeyboard(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager, currentFocus)
                    val city = CityObject(suggestionsAdapter.getItem(position) as Cursor)
                    searchViewCity.setQuery(city.name, false)
                    val geoCord = GeoCoordinate(city.x.toDouble(), city.y.toDouble(), 0.0)
                    loadObjectsOnMap(geoCord)

                    return true
                }

                override fun onSuggestionSelect(position: Int): Boolean {
                    return false
                }

            })
        }
    }

    override fun returnResultAndFinish() {
        val resultIntent = Intent().apply {
            val place = presenter.getPlace(selectedMapMarker)
            if (place != null) {
                putExtra(EXTRA_PLACE, place)
            }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun showSnackbar(messageCode: Int) {
        Snackbar.make(linearLayoutSearchElement, messageCode, Snackbar.LENGTH_LONG).show()
    }

    override fun loadObjectsOnMap(places: Array<Place>) {
        map.removeMapObjects(placesContainer.allMapObjects)
        placesContainer.removeAllMapObjects()
        presenter.clearPlacesMap()

        for (place in places) {
            val title = place.title
            val vicinity = place.vicinity
            val defaultMarker = MapMarker()
            defaultMarker.title = title
            defaultMarker.description = vicinity
            defaultMarker.coordinate = GeoCoordinate(place.position[0], place.position[1], 0.0)
            placesContainer.addMapObject(defaultMarker)
            presenter.savePlaceInMap(defaultMarker, place)

            map.addMapObject(defaultMarker)
        }
    }

    override fun showContacts(contacts: Contacts) {
        if (!contacts.phone.isNullOrEmpty()) {
            textViewPhonePlanElementInfo.text = contacts.phone[0].value
        } else {
            linearLayoutPhone.visibility = View.GONE
        }
        if (!contacts.website.isNullOrEmpty()) {
            textViewWebsitePlanElementInfo.text = contacts.website[0].value
        } else {
            linearLayoutWebsite.visibility = View.GONE
        }
    }

    private fun initializeMap() {
        supportMapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment

        supportMapFragment.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                supportMapFragment.mapGesture.addOnGestureListener(provideOnGestureListener())
                map = supportMapFragment.map

                val gdanskGeoCord = GeoCoordinate(54.339787, 18.609653, 0.0)
                map.setCenter(gdanskGeoCord, Map.Animation.NONE)
                val zoomRate = 0.56
                map.zoomLevel = (map.maxZoomLevel + map.minZoomLevel) * zoomRate
                loadObjectsOnMap(map.center)
            } else {
                showSnackbar(R.string.load_map_error)
            }
        }
    }

    private fun provideOnGestureListener(): MapGesture.OnGestureListener {
        return object : MapGesture.OnGestureListener.OnGestureListenerAdapter() {
            override fun onPanEnd() {
                loadObjectsOnMap(map.center)
            }

            override fun onMapObjectsSelected(objects: List<ViewObject>): Boolean {
                for (viewObj in objects) {
                    if (viewObj.baseType == ViewObject.Type.USER_OBJECT) {
                        if ((viewObj as MapObject).type == MapObject.Type.MARKER) {
                            if (::selectedMapMarker.isInitialized) selectedMapMarker.hideInfoBubble()
                            else {
                                fabCheck.visibility = View.VISIBLE
                                slidingUpPanelSearchElement.panelHeight = resources.getDimension(R.dimen.sliding_up_panel_height).toInt()
                            }

                            // save to remember chosen marker
                            selectedMapMarker = viewObj as MapMarker
                            selectedMapMarker.showInfoBubble()

                            val place = presenter.getPlace(selectedMapMarker)
                            textViewNamePlanElementInfo.text = place.title
                            textViewLocationPlanElementInfo.text = place.vicinity

                            if (place.openingHours != null) textViewOpeningHoursPlanElementInfo.text = place.openingHours.text
                            else linearLayoutOpeningHours.visibility = View.GONE

                            textViewRatingPlanElementInfo.text = place.averageRating
                            presenter.setContacts(place.id, place.href)

                            break
                        }
                    }
                }
                return true
            }
        }
    }

    private fun loadObjectsOnMap(geoCord: GeoCoordinate) {
        map.setCenter(geoCord, Map.Animation.NONE)
        val category = intent.getStringExtra(EXTRA_CATEGORY)
        presenter.search(category!!, map.boundingBox.topLeft.longitude.toString(),
                map.boundingBox.bottomRight.latitude.toString(),
                map.boundingBox.bottomRight.longitude.toString(),
                map.boundingBox.topLeft.latitude.toString())
    }
}
