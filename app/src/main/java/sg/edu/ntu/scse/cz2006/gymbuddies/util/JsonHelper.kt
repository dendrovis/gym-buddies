package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.content.Context
import androidx.annotation.RawRes
import java.io.BufferedReader


/**
 * Helper class to store common methods to handle JSON files
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-17
 */
object JsonHelper {

    /**
     * Reads a JSON file provided by [resId] that is found from a resource in the /res/raw folder
     * @param context Context The application context
     * @param resId Int Raw resource to extract the JSON file from
     * @return String The JSON String
     */
    @JvmStatic
    fun readFromRaw(context: Context, @RawRes resId: Int): String { return context.resources.openRawResource(resId).bufferedReader().use(BufferedReader::readText) }
}