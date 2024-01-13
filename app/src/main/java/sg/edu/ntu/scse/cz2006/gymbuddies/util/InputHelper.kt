package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.app.Activity
import android.view.inputmethod.InputMethodManager


/**
 * Helper class for handling input related tasks
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-16
 */
object InputHelper {

    /**
     * Helper function to hide the Android IME Soft Keyboard for a given [activity]
     * @param activity Activity The activity to close the keyboard for
     */
    @JvmStatic
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isAcceptingText) inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }
}