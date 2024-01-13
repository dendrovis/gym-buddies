package sg.edu.ntu.scse.cz2006.gymbuddies

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_carpark_and_search_result.*
import kotlinx.android.synthetic.main.fragment_cp_details.*
import kotlinx.android.synthetic.main.fragment_gym_details.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import org.apache.commons.text.WordUtils
import sg.edu.ntu.scse.cz2006.gymbuddies.CarparkAndSearchResultActivity.Companion.STATE_CARPARK
import sg.edu.ntu.scse.cz2006.gymbuddies.CarparkAndSearchResultActivity.Companion.STATE_SEARCH
import sg.edu.ntu.scse.cz2006.gymbuddies.CarparkAndSearchResultActivity.Companion.STATE_UNKNOWN
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.CarparkAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.FavGymAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.GymReviewAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.adapter.StringRecyclerAdapter
import sg.edu.ntu.scse.cz2006.gymbuddies.data.CarPark
import sg.edu.ntu.scse.cz2006.gymbuddies.data.GBDatabase
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.*
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.EvaluateCarparkDistance
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.SearchGym
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateCarparkAvailabilityService
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateGymFavourites
import sg.edu.ntu.scse.cz2006.gymbuddies.util.GymHelper
import sg.edu.ntu.scse.cz2006.gymbuddies.util.ProfilePicHelper
import sg.edu.ntu.scse.cz2006.gymbuddies.util.svy21converter.SVY21Coordinate
import sg.edu.ntu.scse.cz2006.gymbuddies.widget.FavButtonView
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Activity for showing both carpark and search results
 *
 * @author Kenneth Soh
 * @since 2019-10-14
 * @property state Int Current state of the activity. Can be either [STATE_SEARCH], [STATE_CARPARK] or [STATE_UNKNOWN]
 * @property mMap GoogleMap The Google Map Object
 * @property backStack Boolean Flag for enabling custom backstack handling for gym details
 * @property backStackCp Boolean Flag for enabling custom backstack handling for carpark details
 * @property autoExpandFlag Boolean Flag for determining if the bottom sheet should auto expand when shown
 * @property gpsPerm Boolean Flag to signify if we have GPS/Location Permission
 * @property gymDetailFavListener ListenerRegistration? Firebase Firestore Listener for Gym Favourites data. This is used to handle real time updates to the favourites count of the gym
 * @property selectedGymUid String? UID of the selected gym
 * @property flagReviewing Boolean Flag to signify if the user is currently reviewing the gym and the gym review dialog is active
 * @property coordinates LatLng? Coordinates of the selected gym for gym directions
 */
class CarparkAndSearchResultActivity : AppCompatActivity(), OnMapReadyCallback {

    private var state = STATE_SEARCH
    private lateinit var mMap: GoogleMap

    /**
     * Internal lifecycle function that is called when the activity is created
     * @param savedInstanceState Bundle? Saved Instance State for configuration changes
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carpark_and_search_result)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        results_list.setHasFixedSize(true)
        val llm = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        results_list.layoutManager = llm
        results_list.itemAnimator = DefaultItemAnimator()

        state = when {
            intent.getBooleanExtra("search", false) -> STATE_SEARCH
            intent.getBooleanExtra("carpark", false) -> STATE_CARPARK
            else -> STATE_UNKNOWN
        }

        val resultsBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val gymBottomSheetBehavior = BottomSheetBehavior.from(gym_details_sheet)
        val cpBottomSheetBehavior = BottomSheetBehavior.from(cp_details_sheet)
        resultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        gymBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        cpBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) gpsPerm = true

        map_view.onCreate(savedInstanceState)
        map_view.getMapAsync(this)
    }

    /**
     * Internal function to display and error message and exit this activity if there is an error
     * @param errorMessage String The error message to display
     */
    private fun errorAndExit(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        finish()
    }

    private var backStack = false
    private var backStackCp = false
    private var autoExpandFlag = false

    /**
     * Internal lifecycle function called when the back button is pressed on the device
     * We override it to properly handle the backstack changes if needed
     */
    override fun onBackPressed() {
        if (backStack) {
            val gymBottomSheetBehavior = BottomSheetBehavior.from(gym_details_sheet)
            if (gymBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED) // Collapse gym details
            else if (gymBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) unselectGymDetails()
        } else if (backStackCp) {
            val cpBottomSheetBehavior = BottomSheetBehavior.from(cp_details_sheet)
            if (cpBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) cpBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED) // Collapse gym details
            else if (cpBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) unselectCpDetails()
        } else super.onBackPressed()
    }

    /**
     * This function is used to initialize the relevant view elements when [state] is [STATE_SEARCH]
     */
    private fun doSearch() {
        supportActionBar?.title = "Search Results"
        val gymBottomSheetBehavior = BottomSheetBehavior.from(gym_details_sheet)
        val resBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val callback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("GymDetailsSheet", "State Changed: $newState")
                bottomSheet.findViewById<View>(R.id.drag_bar).visibility =
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) View.INVISIBLE else View.VISIBLE
                backStack = newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED
                supportActionBar?.title = if (newState == BottomSheetBehavior.STATE_EXPANDED) "View Gym Details" else "Search Results"
                gym_details_title.isSingleLine = newState == BottomSheetBehavior.STATE_COLLAPSED
                if (autoExpandFlag && newState != BottomSheetBehavior.STATE_SETTLING) {
                    autoExpandFlag = false
                    gymBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                if (gymBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN && resBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                    resBottomSheetBehavior.isHideable = true
                    resBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Not used
            }
        }
        gymBottomSheetBehavior.setBottomSheetCallback(callback)
        setupGymDetailsControls()
        // Get filter results
        val paramJson = intent.getStringExtra("searchparam")
        if (paramJson == null) {
            errorAndExit("Search Paramters not found, no results, exiting activity")
            return
        }
        val gson = Gson()
        val param = gson.fromJson(paramJson, GymSearchBy::class.java)
        if (param == null) {
            errorAndExit("Failed to parse params")
            return
        }

        // Check user location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsPerm = true
            callSearchTask(param)
        } else {
            Log.i(TAG, "No permissions, requesting...")
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this).setTitle("Location Permission Required").setMessage("We require access to your location to search for nearby gyms")
                    .setPositiveButton(android.R.string.ok) { _, _ -> ActivityCompat.requestPermissions(this, permissions, RC_LOC_SEARCH) }.show()
            } else ActivityCompat.requestPermissions(this, permissions, RC_LOC_SEARCH)
        }
    }

    private var gpsPerm = false

    /**
     * Internal function to perform gym searching and filtering
     * @param param GymSearchBy Search paramters for gym
     */
    private fun callSearchTask(param: GymSearchBy) {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location == null) return@addOnSuccessListener
            val lastLocation = LatLng(location.latitude, location.longitude)
            zoomToMyLocation(lastLocation)
            SearchGym(this, object: SearchGym.OnComplete { override fun onComplete(result: ArrayList<FavGymObject>) { onSearchResult(result) } }, param, lastLocation).execute()
        }
    }

    /**
     * Function that executes and updates the map and relevant data when search completes
     * @param result ArrayList<FavGymObject> The completed search results. Can be empty for no results matching parameters
     */
    private fun onSearchResult(result: ArrayList<FavGymObject>) {
        val noReviews = arrayOf("No results found from your search")

        if (result.size > 0) {
            val results = FavGymAdapter(result)
            results.setOnClickListener(View.OnClickListener { v ->
                if (v.tag is FavGymAdapter.FavViewHolder) {
                    val obj = (v.tag as FavGymAdapter.FavViewHolder).gymObj as GymList.GymShell
                    showGymDetails()
                    updateGymDetails(obj)
                    zoomToMyLocation(LatLng(obj.geometry.getLat(), obj.geometry.getLng()))
                    autoExpandFlag = true
                }
            })
            results_list.adapter = results
            result.forEach {
                // Add markers for each as well
                val mark = mMap.addMarker(MarkerOptions().position(LatLng(it.gym.geometry.getLat(), it.gym.geometry.getLng())).title(it.gym.properties.Name).snippet(GymHelper.generateAddress(it.gym.properties)))
                mark.tag = it.gym
            }

            val gymBottomSheetBehavior = BottomSheetBehavior.from<View>(gym_details_sheet)
            mMap.setOnInfoWindowClickListener { gymBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED) }
            mMap.setOnMapClickListener { Log.d("mMap", "mapClicked()"); unselectGymDetails() }
            mMap.setOnMarkerClickListener { marker ->
                // Hide and reshow gym
                Log.d("mMap", "markerClicked()")
                showGymDetails()
                if (marker.tag is GymList.GymShell) updateGymDetails(marker.tag as GymList.GymShell?)
                false // We still want to show the info window right now
            }
        } else {
            val noResults = StringRecyclerAdapter(noReviews.toList(), false)
            results_list.adapter = noResults
        }
        updateResultsLayoutHeight()
        loading.visibility = View.GONE
        BottomSheetBehavior.from(bottom_sheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * This function is used to initialize the various view elements if [state] is [STATE_CARPARK]
     */
    private fun doCarpark() {
        supportActionBar?.title = "View Nearby Carparks"
        val gymId = intent.getStringExtra("gym")
        if (gymId == null) {
            errorAndExit("No Gym Selected")
            return
        }

        val gym = GymHelper.getGym(this, gymId)
        if (gym == null) {
            errorAndExit("Gym not found")
            return
        }

        // Get carpark DB
        val carParksDao = GBDatabase.getInstance(application).carParkDao()
        setupCpControls()

        val gymLatLng = LatLng(gym.geometry.getLat(), gym.geometry.getLng())
        EvaluateCarparkDistance(gymLatLng, carParksDao, object: EvaluateCarparkDistance.Callback { override fun onComplete(results: ArrayList<Pair<CarPark, Float>>) { processCarpark(results, gym) } }).execute()

        UpdateCarparkAvailabilityService.updateCarpark(this) // Refresh availability data
    }

    /**
     * Function used to process and retrieve the nearest 10 carparks from the selected gym
     * @param results ArrayList<Pair<CarPark, Float>> List of all carparks and their distance to the selected gym
     * @param gym GymShell Selected gym object
     */
    private fun processCarpark(results: ArrayList<Pair<CarPark, Float>>, gym: GymList.GymShell) {
        // Get first 10 only
        val cp = results.subList(0, 10)
        // Remove any thats greater than 1000m
        val cpFiltered = cp.filter { p -> p.second <= 1000 }
        Log.i(TAG, "Accessible carparks: ${cpFiltered.size}, displaying on map")
        // cp.removeIf { p -> p.second > 1000 }

        // Add markers to map
        mMap.clear()
        // Selected gym as a pin
        // mMap.addMarker(MarkerOptions().position(LatLng(it.gym.geometry.getLat(), it.gym.geometry.getLng())).title(it.gym.properties.Name).snippet(GymHelper.generateAddress(it.gym.properties)))
        val gymLoc = LatLng(gym.geometry.getLat(), gym.geometry.getLng())
        mMap.addMarker(MarkerOptions().position(gymLoc).title(gym.properties.Name).snippet(GymHelper.generateAddress(gym.properties)))
        zoomToMyLocation(gymLoc, 16f)

        // Add all the carparks
        cpFiltered.forEach {
            val svy21 = SVY21Coordinate(it.first.y, it.first.x)
            val latlng = svy21.asLatLon()
            val cpObj = it.first
            val mark = mMap.addMarker(MarkerOptions().position(LatLng(latlng.latitude, latlng.longitude)).title(cpObj.address)
                .snippet("${cpObj.id} | ${it.second} m away").icon(BitmapDescriptorFactory.fromBitmap(ProfilePicHelper.getBitmap(this, R.drawable.ic_parking))))
            mark.tag = it
            Log.d(TAG, "Added ${cpObj.id} to map")
        }
        if (cpFiltered.isNotEmpty()) {
            val adapter = CarparkAdapter(this, cpFiltered)
            adapter.setOnClickListener(View.OnClickListener {
                if (it.tag is CarparkAdapter.CarparkViewHolder) {
                    val holder = it.tag as CarparkAdapter.CarparkViewHolder
                    val pair = Pair(holder.cpObj!!, holder.distance)
                    showCpDetails()
                    displayCarpark(pair)
                    autoExpandFlag = true
                }
            })
            results_list.adapter = adapter
        } else {
            val noCarparks = arrayOf("No carparks found within 1km from the gym")
            val noResults = StringRecyclerAdapter(noCarparks.toList(), false)
            results_list.adapter = noResults
        }
        updateResultsLayoutHeight()
        loading.visibility = View.GONE

        val scale = resources.displayMetrics.density
        val px = (100 * scale + 0.5f).toInt()
        BottomSheetBehavior.from(bottom_sheet).setPeekHeight(px, true)
        //bottom_sheet.requestLayout()
    }

    /**
     * Internal function to update the layout height of the results sheet
     * This will either be to the height of the items or to half of the device screen if there are too many items
     */
    private fun updateResultsLayoutHeight() {
        val scale = resources.displayMetrics.density
        val maxHeight = (450 * scale + 0.5f).toInt()
        bottom_sheet.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val layoutHeight = bottom_sheet.measuredHeight
        Log.d(TAG, "ResListHeight: $layoutHeight | Max Height Limit: $maxHeight")
        val params = bottom_sheet.layoutParams
        if (layoutHeight > maxHeight)
            params.height = maxHeight
        else
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        bottom_sheet.layoutParams = params
        bottom_sheet.requestLayout()
    }

    /**
     * Internal lifecycle function that executes when we receive permission grant/deny details
     * We use this for the location permission
     *
     * @param requestCode Int Permission Request Code for reference
     * @param permissions Array<out String> List of permissions requested
     * @param grantResults IntArray Results of the permission request
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_LOC_SEARCH -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted - initialize the gps source")
                    doSearch()
                    return
                } else errorAndExit("Location Permission not granted, search cannot continue")
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Internal lifecycle function for when a menu item is selected
     * @param item MenuItem The item selected
     * @return Boolean true if complete, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    // GMaps related issues

    /**
     * Internal lifecycle function for resuming an activity
     */
    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }

    /**
     * Internal lifecycle function for pausing an activity
     */
    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    /**
     * Internal lifecycle function that is called when the map is prepared and initalized and ready for use
     * @param p0 GoogleMap The ready Google Maps Object
     */
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.isTrafficEnabled = true
        val settings = mMap.uiSettings
        settings.isMapToolbarEnabled = false
        settings.isMyLocationButtonEnabled = true

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(1.3413054, 103.8074233), 10f))
        mMap.isMyLocationEnabled = gpsPerm

        when (state) {
            STATE_SEARCH -> doSearch()
            STATE_CARPARK -> doCarpark()
            else -> errorAndExit("Unknown action, exiting activity")
        }
    }

    /**
     * Internal method to zoom the map to the user's current location
     */
    private fun zoomToMyLocation(lastLocation: LatLng, zoom: Float = 15f) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, zoom))
    }

    // Carpark Related
    /**
     * Internal function to setup carpark details bottom sheet
     */
    private fun setupCpControls() {
        val cpBottomSheetBehavior = BottomSheetBehavior.from(cp_details_sheet)
        val resBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val callback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("CpDetailsSheet", "State Changed: $newState")
                bottomSheet.findViewById<View>(R.id.drag_bar).visibility = if (newState == BottomSheetBehavior.STATE_EXPANDED) View.INVISIBLE else View.VISIBLE
                backStackCp = newState == BottomSheetBehavior.STATE_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED
                cp_details_title.isSingleLine = newState == BottomSheetBehavior.STATE_COLLAPSED
                if (autoExpandFlag && newState != BottomSheetBehavior.STATE_SETTLING) {
                    autoExpandFlag = false
                    cpBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                if (cpBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN && resBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                    resBottomSheetBehavior.isHideable = true
                    resBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Not used
            }
        }
        cpBottomSheetBehavior.setBottomSheetCallback(callback)

        mMap.setOnInfoWindowClickListener { cpBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED) }
        mMap.setOnMapClickListener { Log.d("mMap", "mapClicked()"); unselectCpDetails() }
        mMap.setOnMarkerClickListener { marker ->
            // Hide and reshow gym
            Log.d("mMap", "markerClicked()")
            @Suppress("UNCHECKED_CAST")
            if (marker.tag != null && marker.tag is Pair<*, *>) {
                showCpDetails()
                displayCarpark(marker.tag as Pair<CarPark, Float>)
            } else unselectCpDetails()
            false // We still want to show the info window right now
        }
    }

    /**
     * Internal function called to hide the gym details bottom sheet and redisplay the favourites list bottom sheet
     */
    private fun unselectCpDetails() {
        val resultsBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val cpBottomSheetBehavior = BottomSheetBehavior.from(cp_details_sheet)
        resultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        resultsBottomSheetBehavior.isHideable = false
        cpBottomSheetBehavior.isHideable = true
        cpBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        if (gymDetailFavListener != null) {
            gymDetailFavListener!!.remove()
            gymDetailFavListener = null
        }
    }

    /**
     * Internal function called to hide the favourites list bottom sheet and display the gym details bottom sheet
     */
    private fun showCpDetails() {
        val resultsBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val cpBottomSheetBehavior = BottomSheetBehavior.from(cp_details_sheet)
        cpBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        cpBottomSheetBehavior.isHideable = false
        resultsBottomSheetBehavior.isHideable = true
        resultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    /**
     * Displays the carpark information on the carpark bottom sheet
     * @param cp Pair<CarPark, Float> Carpark object with its distance to the selected gym
     */
    private fun displayCarpark(cp: Pair<CarPark, Float>) {
        cp_details_title.text = cp.first.address
        cp_details_distance.text = "${cp.second}m away"
        cp_details_id.text = cp.first.id
        cp_details_address.text = cp.first.address
        cp_details_height.text = if (cp.first.gantryHeight > 0) "${cp.first.gantryHeight}m height limit" else "No height limit"
        cp_details_rate.text = "${cp.first.shortTermParking} HOURLY PARKING\n${if (cp.first.freeParking == "NO") "NO FREE PARKING" else 
            "FREE PARKING FOR ${cp.first.freeParking}"}\n${if (cp.first.nightParking == "NO") "NO" else ""}NIGHT PARKING AVAILABLE "
        cp_details_misc.text = "${cp.first.carParkType}\n${cp.first.systemType} ${if (cp.first.basement=="N") "" else "\nBASEMENT PARKING AVAILABLE"} ${if (cp.first.decks > 0) "\n${cp.first.decks} decks available" else ""}"

        cp_details_rate.text = WordUtils.capitalizeFully(cp_details_rate.text.toString(), ' ', '\n')
        cp_details_misc.text = WordUtils.capitalizeFully(cp_details_misc.text.toString(), ' ', '\n')

        // Add HDB Rates to cp_details_rate cause by right all HDB
        val centralCarparks = arrayListOf("HLM", "KAB", "KAM", "KAS", "PRM", "SLS", "SR1", "SR2", "TPM", "UCS") // These carparks are carparks that charges higher according to HDB site
        val peakCarparks = getPeakCarparks();
        if (cp.first.shortTermParking != "NO") {
            // Has Hourly Parking, Display Hourly Parking rates
            var rates = "Carpark Rates:\nCars: ${if (centralCarparks.contains(cp.first.id)) "$1.20 per half hour (7AM-5PM, Mon-Sat),\n" else ""}$0.60 per " +
                    "half hour${if (centralCarparks.contains(cp.first.id)) " (other hours)" else ""}"
            if (peakCarparks.containsKey(cp.first.id)) rates += "\nPeak: ${peakCarparks[cp.first.id]}"
            rates += "\nMotocycles: $0.65 per lot"
            cp_details_rate.text = cp_details_rate.text.toString() + "\n\n$rates"
        }

        // Get data from file
        val jsonFile = File(cacheDir, "avail.txt")
        val json = jsonFile.readText()
        val gson = Gson()
        val arr = gson.fromJson<Array<CarparkAvailability>>(json, Array<CarparkAvailability>::class.java)
        val hmap = HashMap<String, CarparkAvailability>()
        arr.forEach { hmap[it.CarParkID] = it }
        if (hmap.containsKey(cp.first.id)) {
            cp_details_lots.text = "Lots Available: ${hmap[cp.first.id]?.AvailableLots ?: "Unknown"}"
        } else cp_details_lots.text = "No Lots Availability Information Found"

        val coordinates = SVY21Coordinate(cp.first.y, cp.first.x).asLatLon()
        cp_details_direction.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?daddr=" +
                if (coordinates == null) cp_details_address.text.toString() else "${coordinates.latitude},${coordinates.longitude}"))) }
    }

    /**
     * Internal function used to get all HDB carparks as per data from https://www.hdb.gov.sg/cs/infoweb/car-parks/short-term-parking/short-term-parking-charges
     * @return HashMap<String, String> Hashmap of all peak hour carpark data
     */
    private fun getPeakCarparks(): HashMap<String, String> {
        return hashMapOf(
            Pair("ACB", "$1.40 per half hour (10AM-10:30PM daily)"),
            Pair("B6", "$0.80 per half hour (10AM-8PM daily)"),
            Pair("B6M", "$0.80 per half hour (10AM-8PM daily)"),
            Pair("B7", "$0.80 per half hour (10AM-8PM daily)"),
            Pair("KB10", "$0.80 per half hour (7AM-2PM, Mon-Sat)"),
            Pair("KB11", "$0.80 per half hour (7AM-2PM, Mon-Sat)"),
            Pair("KB12", "$0.80 per half hour (7AM-2PM, Mon-Sat)"),
            Pair("BBB", "$1.40 per half hour (10AM-10:30PM daily)"),
            Pair("CY", "$1.40 per half hour (10AM-10:30PM daily)"),
            Pair("GSM", "$0.80 per half hour (7AM-10:30PM, Fri, Sat & PH)"),
            Pair("CR1", "$0.80 per half hour (10AM-10:30PM daily)"),
            Pair("HG10", "$0.80 per half hour (11AM-2PM, Mon-Fri/7AM-2PM, Sat, Sun & PH)"),
            Pair("HG95", "$0.80 per half hour (11AM-2PM, Mon-Fri/7AM-2PM, Sat, Sun & PH)"),
            Pair("HG55", "$0.80 per half hour (10AM-10:30PM, Mon-Fri/7AM-10:30PM, Sat, Sun & PH)"),
            Pair("JCM", "$0.80 per half hour (7AM-10:30PM, Fri-Sun & PH)"),
            Pair("HG9", "$0.80 per half hour (11AM-2PM/6PM-9PM, Mon-Fri/11AM-9PM, Sat, Sun & PH)"),
            Pair("HG16", "$0.80 per half hour (11AM-2PM/6PM-9PM, Mon-Fri/11AM-9PM, Sat, Sun & PH)"),
            Pair("HG9T", "$0.80 per half hour (11AM-2PM/6PM-9PM, Mon-Fri/7AM-9PM, Sat, Sun & PH)"),
            Pair("HG15", "$0.80 per half hour (11AM-2PM/6PM-9PM, Mon-Fri/7AM-5PM, Sat, Sun & PH)"),
            Pair("HG25", "$0.80 per half hour (11AM-2PM/6PM-9PM, Mon-Fri/11AM-2PM, Sat, Sun & PH)"),
            Pair("MP14", "$0.80 per half hour (8AM-8PM daily)"),
            Pair("MP15", "$0.80 per half hour (8AM-8PM daily)"),
            Pair("MP16", "$0.80 per half hour (8AM-8PM daily)"),
            Pair("MP19", "$0.80 per half hour (10AM-2PM, Fri-Sun & PH)"),
            Pair("SE20", "$0.80 per half hour (5PM-10:30PM daily)"),
            Pair("SE24", "$0.80 per half hour (5PM-10:30PM daily)"),
            Pair("SE21", "$0.80 per half hour (5PM-10:30PM, Mon-Sat)"),
            Pair("SE22", "$0.80 per half hour (5PM-10:30PM, Mon-Sat)"),
            Pair("BRB1", "$1.40 per half hour (8AM-6PM daily)"),
            Pair("DUXM", "$1.40 per half hour (7AM-5PM, Mon-Sat)"),
            Pair("WCB", "$1.40 per half hour (10AM-10:30PM daily)"),
            Pair("BJ14", "$0.80 per half hour (7AM-10:30PM daily)")
        )
    }

    // Search related

    /**
     * Internal function called to hide the gym details bottom sheet and redisplay the favourites list bottom sheet
     */
    private fun unselectGymDetails() {
        val resultsBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val gymDetailsBottomSheetBehavior= BottomSheetBehavior.from(gym_details_sheet)
        resultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        resultsBottomSheetBehavior.isHideable = false
        gymDetailsBottomSheetBehavior.isHideable = true
        gymDetailsBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        if (gymDetailFavListener != null) {
            gymDetailFavListener!!.remove()
            gymDetailFavListener = null
        }
    }

    /**
     * Internal function called to hide the favourites list bottom sheet and display the gym details bottom sheet
     */
    private fun showGymDetails() {
        val resultsBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val gymDetailsBottomSheetBehavior= BottomSheetBehavior.from(gym_details_sheet)
        gymDetailsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        gymDetailsBottomSheetBehavior.isHideable = false
        resultsBottomSheetBehavior.isHideable = true
        resultsBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    /**
     * Initialize method for setting up the gym details bottom sheet
     */
    private fun setupGymDetailsControls() {
        gym_details_location.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?daddr=" +
                if (coordinates == null) gym_details_location.text.toString() else "${coordinates!!.latitude},${coordinates!!.longitude}"))) }

        // Rating Stuff
        val editRatingBar = gym_details_rate_write
        val profilePic = profile_pic
        if (ProfilePicHelper.profilePic != null) profilePic.setImageDrawable(ProfilePicHelper.profilePic)
        else ProfilePicHelper.profilePicUpdateListener.add(object: ProfilePicHelper.ProfilePicOneTimeListener { override fun onUpdate(drawable: RoundedBitmapDrawable?) { profile_pic.setImageDrawable(drawable) } })

        editRatingBar.setOnRatingChangeListener { ratingBar, rating->
            if (flagReviewing) { flagReviewing = false; return@setOnRatingChangeListener }
            val review = layoutInflater.inflate(R.layout.dialog_review, null)
            val bar = review.findViewById<MaterialRatingBar>(R.id.gym_details_rate_write)
            bar.rating = rating
            val reviewMessage = review.findViewById<TextInputEditText>(R.id.gym_details_review)
            val profilePics = review.findViewById<ImageView>(R.id.profile_pic)
            if (ProfilePicHelper.profilePic != null) profilePics.setImageDrawable(ProfilePicHelper.profilePic)
            else ProfilePicHelper.profilePicUpdateListener.add(object: ProfilePicHelper.ProfilePicOneTimeListener { override fun onUpdate(drawable: RoundedBitmapDrawable?) { profilePics.setImageDrawable(drawable) } })
            AlertDialog.Builder(gym_details_sheet.context).setTitle("Feedback about Gym").setCancelable(false)
                .setView(review).setPositiveButton("Submit") { _, _ -> submitReview(bar, reviewMessage) }
                .setNeutralButton(android.R.string.cancel) { _, _ -> flagReviewing = true; ratingBar.rating = 0f }.show() }

        review_recycler.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        review_recycler.layoutManager = llm
        review_recycler.itemAnimator = DefaultItemAnimator()

         // On Click
        gym_details_nearby_carparks_btn.setOnClickListener { Snackbar.make(coordinator, R.string.coming_soon_feature, Snackbar.LENGTH_LONG).show() }
        gym_details_fav.setOnClickListener { gym_details_fav_icon.callOnClick() }
        gym_details_fav_icon.setOnClickListener { v-> if (v is FavButtonView) {
            v.onClick(v) // Execute existing view onclick listener
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                UpdateGymFavourites(this, user.uid, selectedGymUid, v.isChecked, object: UpdateGymFavourites.Callback {
                    override fun onComplete(success: Boolean) {
                        if (success) Snackbar.make(coordinator, if ((v.isChecked)) "Saved to favourites!" else "Removed from favourites!", Snackbar.LENGTH_SHORT).show()
                        else Snackbar.make(coordinator, if ((v.isChecked)) "Failed to save to favourites. Try again later" else "Failed to remove from favourites. Try again later", Snackbar.LENGTH_SHORT).show() }
                    }).execute()
            } }
        }

        gym_details_nearby_carparks_btn.setOnClickListener { view ->
            val i = Intent(view.getContext(), CarparkAndSearchResultActivity::class.java)
            i.putExtra("carpark", true)
            i.putExtra("gym", selectedGymUid)
            startActivity(i)
        }
    }

    private var gymDetailFavListener: ListenerRegistration? = null
    private var selectedGymUid: String? = null
    private var flagReviewing: Boolean = false
    private var coordinates: LatLng? = null

    /**
     * Updates the data in the gym details bottom sheet
     * @param gym The gym whose data we are updating the sheet with
     */
    private fun updateGymDetails(gym: GymList.GymShell?) {
        if (gym == null) return
        gym_details_title.text = gym.properties.Name
        gym_details_description.text = gym.properties.description
        if (gym_details_description.text.toString().trim { it <= ' ' }.isEmpty()) gym_details_description.text = "No description available"
        gym_details_location.text = GymHelper.generateAddress(gym.properties)
        coordinates = LatLng(gym.geometry.getLat(), gym.geometry.getLng())
        gym_details_fav_icon.isChecked = false
        selectedGymUid = gym.properties.INC_CRC
        // Initial update
        val gymRef = FirebaseFirestore.getInstance().collection(GymHelper.GYM_COLLECTION).document(gym.properties.INC_CRC)
        /*
        if (currentUserFavList.size() > 0 && currentUserFavList.containsKey(gym.getProperties().getINC_CRC())) {
            heartIcon.setChecked(true);
            Integer favCount = currentUserFavList.get(gym.getProperties().getINC_CRC());
            this.favCount.setText(getResources().getString(R.string.number_counter, favCount));
        }
         */
        gymRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val tmp = documentSnapshot.toObject(FavGymFirestore::class.java)
                tmp?.let {
                    gym_details_fav_count.text = resources.getString(R.string.number_counter, it.count)
                    val userid = FirebaseAuth.getInstance().uid
                    if (it.userIds.contains(userid)) gym_details_fav_icon.isChecked = true
                }
            }
            else gym_details_fav_count.text = "(0)"
        }.addOnFailureListener { gym_details_fav_count.text = "(?)" }

        // Register update
        if (gymDetailFavListener != null) gymDetailFavListener!!.remove()
        gymDetailFavListener = gymRef.addSnapshotListener { documentSnapshot, _ ->
            if (documentSnapshot != null && documentSnapshot.exists()) gym_details_fav_count.text = resources.getString(R.string.number_counter, Integer.parseInt(documentSnapshot.get("count")!!.toString()))
            else gym_details_fav_count.setText("(0)")
        }

        // DEBUG SETTINGS
        gym_details_debug_value.text = gym.properties.INC_CRC
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        gym_details_debug_layout.visibility = if (sp.getBoolean("debug_mode", false)) View.VISIBLE else View.GONE

        updateGymRatings()
    }

    /**
     * Internal function to update gym ratings for a selected gym
     */
    private fun updateGymRatings() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e(TAG, "User not logged in, cannot get ratings and stuff")
            return  // We stop here
        }
        setGymStatus(false, null, 0f) // Default to no reviews while we are downloading
        if (gym_details_rate_write.rating > 0f) {
            flagReviewing = true
            gym_details_rate_write.rating = 0f
        }
        val db = FirebaseFirestore.getInstance()
        // Get your user's rating list if any
        if (selectedGymUid == null) return
        db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid!!).collection(GymHelper.GYM_USERS_COLLECTION).document(user.uid).get().addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) setGymStatus(false, null, 0f) // No reviews from user
                else {
                    val rating = documentSnapshot.toObject(FirestoreRating::class.java)
                    if (rating != null) setGymStatus(true, rating.message, rating.rating)
                    else Toast.makeText(this, "Failed to get your ratings (ObjectCastError)", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e -> Toast.makeText(this, "Failed to get ratings (" + e.localizedMessage + ")", Toast.LENGTH_LONG).show() }
        // Set loading status for review while we retrieve the needed data (double calls boo)
        val loading = arrayOf("Loading Reviews for gym...")
        val adapter = StringRecyclerAdapter(loading.toList(), false)
        review_recycler.adapter = adapter

        // Init for review count, rating and list
        gym_details_review_count.text = "(...)"
        gym_details_review_count_general.text = "(...)"
        gym_details_rate_bar.rating = 0f
        gym_details_rate_avg.text = "-"

        if (selectedGymUid == null) return
        db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid!!)
            .collection(GymHelper.GYM_USERS_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { querySnapshot ->
                val reviewDocList = querySnapshot.documents
                if (reviewDocList.size <= 0) {
                    // No reviews
                    val noReviews = arrayOf("No Reviews Found for this gym. Make one now!")
                    adapter.updateStrings(noReviews.toList())
                    adapter.notifyDataSetChanged()
                    gym_details_review_count.text = "(0)"
                    gym_details_review_count_general.text = "(0)"
                    return@addOnSuccessListener
                }
                // Get user list as well
                db.collection(GymHelper.GYM_USERS_COLLECTION).get().addOnSuccessListener { querySnapshot1 ->
                    val userList = HashMap<String, User>()
                    querySnapshot1.documents.forEach { d -> userList[d.id] = d.toObject(User::class.java)!! } // Parse user objects into hashmap

                    // Create the recycler items based off the reviews and users data
                    val reviewList = java.util.ArrayList<GymRatings>()
                    var overallGymRating = 0.0f
                    reviewDocList.forEach {
                        if (!userList.containsKey(it.id)) return@forEach // Deleted user, Don't bother with their reviews
                        val fr = it.toObject(FirestoreRating::class.java)
                        val u = userList[it.id]
                        if (fr == null || u == null) return@forEach // Error occurred, dont add that
                        reviewList.add(GymRatings(it.id, fr, u))
                        overallGymRating += fr.rating
                    }

                    val reviewAdapter = GymReviewAdapter(this, reviewList)
                    review_recycler.adapter = reviewAdapter
                    gym_details_review_count.text = "(${reviewList.size})"
                    gym_details_review_count_general.text = "(${reviewList.size})"
                    // Get overall rating and stuff
                    val averageGymRating = overallGymRating / reviewList.size
                    gym_details_rate_bar.rating = averageGymRating
                    gym_details_rate_avg.text = String.format(Locale.US, "%.2f", averageGymRating)
                    }.addOnFailureListener { Log.e(TAG, "Failed to get review users"); Toast.makeText(this, "Failed to get rating list (users)", Toast.LENGTH_LONG).show() }
            }.addOnFailureListener { Toast.makeText(this, "Failed to get rating list (list)", Toast.LENGTH_LONG).show(); Log.e(TAG, "Failed to get gym review list") }
    }

    /**
     * Internal function to set the logged in user's gym review status
     * This will determine if the user has a review for that particular gym or not.
     * If there is, the view review layout is used, otherwise the create new review layout is used
     *
     * @param hasReview User currently has a review for the selected gym if true, false otherwise
     * @param message Any review message in the user's review of the gym, null for empty
     * @param rating The rating for the gym given by the user. Will be 0 by default
     */
    private fun setGymStatus(hasReview: Boolean, message: String?, rating: Float) {
        val transition = Fade()
        transition.duration = 300
        transition.addTarget(gym_details_rate_edit).addTarget(gym_details_rate_view)

        TransitionManager.beginDelayedTransition(gym_details_sheet as ViewGroup, transition)
        gym_details_rate_edit.visibility = if (hasReview) View.GONE else View.VISIBLE
        gym_details_rate_view.visibility = if (hasReview) View.VISIBLE else View.GONE
        if (hasReview) {
            // Update view mode
            gym_details_rate_read.rating = rating
            gym_details_review_readonly.text = message ?: ""

            // We set the onclick here as only here we can easily access the message and rating
            val editReview = gym_details_rate_edit_btn
            editReview.setOnClickListener {
                val review1 = layoutInflater.inflate(R.layout.dialog_review, null)
                val bar = review1.findViewById<MaterialRatingBar>(R.id.gym_details_rate_write)
                bar.rating = rating
                val reviewMessage = review1.findViewById<TextInputEditText>(R.id.gym_details_review)
                reviewMessage.setText(message)
                val profilePics = review1.findViewById<ImageView>(R.id.profile_pic)
                if (ProfilePicHelper.profilePic != null) profilePics.setImageDrawable(ProfilePicHelper.profilePic)
                else ProfilePicHelper.profilePicUpdateListener.add(object: ProfilePicHelper.ProfilePicOneTimeListener {
                    override fun onUpdate(drawable: RoundedBitmapDrawable?) { profilePics.setImageDrawable(drawable) }
                })
                AlertDialog.Builder(it.context).setTitle("Feedback about Gym").setCancelable(false)
                    .setView(review1).setPositiveButton("Submit") { _, _ -> submitReview(bar, reviewMessage) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton("Delete") { _, _ ->
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            Snackbar.make(gym_details_sheet, "Error deleting review. Please relogin", Snackbar.LENGTH_LONG).show()
                            return@setNeutralButton
                        }
                        val db = FirebaseFirestore.getInstance()
                        val ref = db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid!!).collection(GymHelper.GYM_USERS_COLLECTION).document(user.uid)
                        ref.delete().addOnSuccessListener {
                            Snackbar.make(gym_details_sheet, "Review deleted successfully!", Snackbar.LENGTH_LONG).show()
                            setGymStatus(false, null, 0f)
                            flagReviewing = true
                            gym_details_rate_write.rating = 0f
                        }.addOnFailureListener { e -> Snackbar.make(gym_details_sheet, "Failed to delete review (" + e.localizedMessage + ")", Snackbar.LENGTH_LONG).show() }
                    }.show()
            }
        }


        // Update the rating after a delay
        Handler().postDelayed({
            FirebaseFirestore.getInstance().collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid!!).get().addOnSuccessListener { documentSnapshot ->
                val stats = documentSnapshot.toObject(GymRatingStats::class.java)
                val user = FirebaseAuth.getInstance().currentUser
                if (stats == null || user == null) return@addOnSuccessListener
                gym_details_rate_bar.rating = stats.averageRating
                gym_details_rate_avg.text = String.format(Locale.US, "%.2f", stats.averageRating)
                gym_details_review_count_general.text = "(" + stats.count + ")"

                // Update results list as well
                val adapter = results_list.adapter as FavGymAdapter
                val index = adapter.getList().indexOfFirst { predicate -> predicate.gym.properties.INC_CRC == selectedGymUid }
                if (index >= 0) {
                    adapter.getList()[index].avgRating = stats.averageRating
                    adapter.getList()[index].ratingCount = stats.count
                }

                adapter.notifyDataSetChanged()
            }
        }, 5000) // Update after 5 seconds
    }

    /**
     * Internal function to submit review for the selected gym
     * @param bar The rating bar object that stores the gym rating that the user has selected
     * @param reviewMessage The review message TextInputEditText object that stores any review of the gym that the user is currently rating
     */
    private fun submitReview(bar: MaterialRatingBar, reviewMessage: TextInputEditText) {
        val rateValue = bar.rating
        val reviewValue = if (reviewMessage.text == null) "" else reviewMessage.text!!.toString().trim()
        // Attempt to detect any error messsages
        val error = if (reviewValue.length > 512) "Review message is too long" else null
        val frate = FirestoreRating(rateValue, reviewValue, System.currentTimeMillis())
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) { Snackbar.make(gym_details_sheet, "Error submitting review. Please relogin", Snackbar.LENGTH_LONG).show(); return }
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection(GymHelper.GYM_REVIEWS_COLLECTION).document(selectedGymUid!!).collection(GymHelper.GYM_USERS_COLLECTION).document(user.uid)
        ref.set(frate).addOnSuccessListener {
            Snackbar.make(gym_details_sheet, "Review submitted successfully!", Snackbar.LENGTH_LONG).show()
            // Update the main view as well and replace edit mode with view mode
            setGymStatus(true, reviewValue, rateValue)
        }.addOnFailureListener { e ->
            Snackbar.make(gym_details_sheet, "Failed to submit review (" + (error ?: e.localizedMessage) + ")", Snackbar.LENGTH_LONG).show()
            Log.e(TAG, "Failed to submit review (" + e.localizedMessage + ")")
        }
    }

    companion object {
        /**
         * Internal tag for logging purposes
         */
        private const val TAG = "CarparkAndSearch"
        /**
         * Location permission request code for search
         */
        private const val RC_LOC_SEARCH = 1
        /**
         * Activity is currently in gym searching state
         */
        const val STATE_SEARCH = 0
        /**
         * Activity is currently in gym nearby carpark state
         */
        const val STATE_CARPARK = 1
        /**
         * Activity is currently in an unknown state and will throw an error
         */
        const val STATE_UNKNOWN = -1
    }
}
