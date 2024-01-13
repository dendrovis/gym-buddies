package sg.edu.ntu.scse.cz2006.gymbuddies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_login_chooser.*
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.CheckFirstRun
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.UpdateCarparkAvailabilityService

/**
 * This activity is just used to ensure that we have processed everything from our database that we need to prevent
 *
 * This checks that
 * - The user has completed their first run sequence (MUST BE AUTHENTICATED)
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-16
 */
class UpdateUserActivity : AppCompatActivity() {

    /**
     * Function that is called when an activity is created
     * @param savedInstanceState Bundle? The Android saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_chooser)

        val auth = FirebaseAuth.getInstance().currentUser
        if (auth == null) {
            Log.e(TAG, "User not valid, exiting")
            startActivity(Intent(this, LoginChooserActivity::class.java))
            finish()
            return
        }

        message.text = "Updating user data..."

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (!sp.contains("nearby-gyms")) sp.edit().putInt("nearby-gyms", 10).apply() // Default to 10 nearby gyms

        // Do retreival of remote config files
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config)
        remoteConfig.fetchAndActivate().addOnSuccessListener {
            Log.i(TAG, "Fetched latest settings")
            Log.d(TAG, "LTA Key: ${remoteConfig.getString("lta_datamall_api_key")}")
            sp.edit().putString("ltakey", remoteConfig.getString("lta_datamall_api_key")).apply()
        }.addOnFailureListener{
            Log.e(TAG, "Failed to fetch from Remote Config. Exception: ${it.localizedMessage}")
        }

        CheckFirstRun(this, object: CheckFirstRun.Callback {
            override fun isFirstRun(success: Boolean) {
                Log.d(TAG, "isFirstRun: $success")
                if (success) goEditProfile() else startActivity(Intent(this@UpdateUserActivity, MainActivity::class.java))
                finish()
            }

            override fun isError() {
                Log.w(TAG, "Error detected, logging out")
                val logout = Intent(this@UpdateUserActivity, LoginChooserActivity::class.java).apply { putExtra("logout", true) }
                startActivity(logout)
                finish()
            }

        }).execute(auth.uid)
        return
    }

    /**
     * Lifecycle method when activity resumes
     */
    override fun onResume() {
        super.onResume()
        UpdateCarparkAvailabilityService.updateCarpark(this)
    }

    /**
     * Internal function that is called when the user is a new user and has not setup their profile yet to go over to the [ProfileEditActivity] activity
     */
    private fun goEditProfile() {
        val intent = Intent(this, ProfileEditActivity::class.java).apply {
            putExtra("firstrun", true)
        }
        startActivity(intent)
    }

    companion object {
        /**
         * Activity Tag for logs
         */
        private const val TAG = "ProfileCheck"
    }
}
