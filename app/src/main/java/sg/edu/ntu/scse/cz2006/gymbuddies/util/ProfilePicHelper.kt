package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL


/**
 * Helper class for handling profile pictures in the application
 *
 * For sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-09
 */
object ProfilePicHelper {

    /**
     * Generates a bitmap given a [url] to the image to download and generate
     * @param url String URL of the image to download and generate a bitmap of
     * @return Bitmap? The bitmap of the image you are downloading
     */
    @JvmStatic
    fun getImageBitmap(url: String): Bitmap? {
        var bm: Bitmap? = null
        try {
            val aURL = URL(url)
            val conn = aURL.openConnection()
            conn.connect()
            val `is` = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close()
        } catch (e: IOException) {
            Log.e("ProfilePicHelper", "Error getting bitmap", e)
        }

        return bm
    }

    /**
     * Internal method to generate a bitmap from a [vectorDrawable]
     * @param vectorDrawable VectorDrawable Vector Drawable object
     * @return Bitmap The generated bitmap
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        vectorDrawable.draw(canvas)
        return bitmap
    }

    /**
     * Internal method to generate a bitmap from a [vectorDrawable]
     * @param vectorDrawable VectorDrawableCompat Vector Drawable object that is compatible with AppCompat
     * @return Bitmap The generated bitmap
     */
    @JvmStatic
    private fun getBitmap(vectorDrawable: VectorDrawableCompat): Bitmap {
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        vectorDrawable.draw(canvas)
        return bitmap
    }

    /**
     * Generates a bitmap from a [drawableResId] in the app drawable folder. Requires an application [context] to use
     * @param context Context Application Context
     * @param drawableResId Int Resource ID for the drawable to generate the bitmap of
     * @return Bitmap The generated bitmap
     */
    @JvmStatic
    fun getBitmap(context: Context, @DrawableRes drawableResId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableResId)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawableCompat) {
            getBitmap((drawable as VectorDrawableCompat?)!!)
        } else if (drawable is VectorDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getBitmap((drawable as VectorDrawable?)!!)
        } else {
            throw IllegalArgumentException("Unsupported drawable type")
        }
    }

    /**
     * A global profile picture storeage area
     */
    @JvmStatic
    var profilePic: RoundedBitmapDrawable? = null
        set(value) { field = value; updateCall()}

    /**
     * Add to this list if you wish to receive an update when the profile pic updates
     */
    @JvmStatic
    val profilePicUpdateListener: ArrayList<ProfilePicOneTimeListener> = ArrayList()

    /**
     * Internal method called after updating the profile pic
     */
    @JvmStatic
    private fun updateCall() { profilePicUpdateListener.forEach { it.onUpdate(profilePic) }; profilePicUpdateListener.clear() }

    /**
     * Internal interface for Profile Picture One Time Listener
     */
    interface ProfilePicOneTimeListener {
        /**
         * Updates profile piture function
         * @param drawable RoundedBitmapDrawable? Drawable to update with
         */
        fun onUpdate(drawable: RoundedBitmapDrawable?)
    }

}