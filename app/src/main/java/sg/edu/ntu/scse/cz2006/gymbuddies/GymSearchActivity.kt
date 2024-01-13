package sg.edu.ntu.scse.cz2006.gymbuddies

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_gym_search.*
import kotlinx.android.synthetic.main.row_pref_rating.*
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymSearchBy

/**
 * Activity used to search for a gym with parameters
 *
 * @author Kenneth Soh
 * @since 2019-10-14
 */
class GymSearchActivity : AppCompatActivity() {

    /**
     * Internal lifecycle function when the activity is created
     * @param savedInstanceState Bundle? The saved instance state for configuration changes
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gym_search)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab.setOnClickListener {
            val params = getParam() ?: return@setOnClickListener
            val paramsJson = Gson().toJson(params)
            val intent = Intent(this, CarparkAndSearchResultActivity::class.java).apply {
                putExtra("search", true)
                putExtra("searchparam", paramsJson)
            }
            startActivity(intent)
        }

        // Default set all to checked
        cb_rateall.isChecked = true
    }

    /**
     * Retrieves all parameters for view elements and store it in the gym search paramter object
     * @return GymSearchBy? The gym search parameter object
     */
    private fun getParam(): GymSearchBy? {
        til_etDist.isErrorEnabled = false
        val dist = try { etDistance.text.toString().toDouble() } catch (e: NumberFormatException) { 0.0 }
        if (dist <= 0) {
            til_etDist.error = "Distance cannot be less than or equals to 0"
            til_etDist.isErrorEnabled = true
            return null
        }

        val selectedOrderId = radio_order.checkedRadioButtonId
        val selectedRatingId = radio_pref_rating.checkedRadioButtonId
        val order = when (selectedOrderId) {
            R.id.rb_abc -> GymSearchBy.SORT_ABC_ASC
            R.id.rb_xyz -> GymSearchBy.SORT_ABC_DSC
            R.id.rb_popz -> GymSearchBy.SORT_FAV_ASC
            R.id.rb_unpopz -> GymSearchBy.SORT_FAV_DSC
            else -> GymSearchBy.SORT_ABC_ASC
        }
        val rating = when (selectedRatingId) {
            R.id.cb_rate0 -> 0
            R.id.cb_rate1 -> 1
            R.id.cb_rate2 -> 2
            R.id.cb_rate3 -> 3
            R.id.cb_rate4 -> 4
            R.id.cb_rate5 -> 5
            R.id.cb_rateall -> -1
            else -> -1
        }

        return GymSearchBy(order, dist, rating)
    }

    /**
     * Internal lifecycle function for when menu item is selected
     * @param item MenuItem The selected menu item
     * @return Boolean true if success, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) { finish(); true }
        else super.onOptionsItemSelected(item)
    }
}
