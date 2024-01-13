package sg.edu.ntu.scse.cz2006.gymbuddies.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymRatings
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DiskIOHelper
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Recycler Adapter for Gym Reviews
 * For sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-12
 * @property reviewList List<GymRatings> The list of all reviews for the specific gym
 * @constructor Creates a adapter for the Gym Reviews List RecyclerView
 */
class GymReviewAdapter(activity: Activity, reviews: List<GymRatings>) : RecyclerView.Adapter<GymReviewAdapter.ReviewViewHolder>() {

    private var reviewList: List<GymRatings> = ArrayList()
    private val actRef = WeakReference(activity)

    init { this.reviewList = reviews }

    /**
     * Gets the number of gyms stored in this adapter
     * @return Int Number of gyms in this adapter
     */
    override fun getItemCount(): Int {
        return reviewList.size
    }

    /**
     * Gets the gym list stored in this adapter
     * @return List<GymRatings> Gym Review object
     */
    fun getList(): List<GymRatings> { return reviewList }

    /**
     * Internal function to bind the view [holder] at the current RecyclerView [position] to a specific data
     * @param holder FavViewHolder Holder to store the data
     * @param position Int Positing in the RecyclerView
     */
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val s = reviewList[position]
        holder.name.text = s.userObj.name
        holder.rating.rating = s.ratingObj.rating
        holder.review.text = s.ratingObj.message

        val sdf = SimpleDateFormat("dd/MM/yy", Locale.US)
        val dt = Date().apply { time = s.ratingObj.timestamp }
        holder.date.text = sdf.format(dt)

        val activity = actRef.get() ?: return
        if (s.userObj.profilePicUri.isNotEmpty() && s.userObj.profilePicUri != "null")
            if (DiskIOHelper.hasImageCache(activity, s.userObj.uid)) {
                Log.i("GymAdapter", "Found Cached Profile Pic for ${s.userObj.uid}")
                val roundBitmap = RoundedBitmapDrawableFactory.create(activity.resources, DiskIOHelper.readImageCache(activity, s.userObj.uid))
                roundBitmap.isCircular = true
                holder.profilePic.setImageDrawable(roundBitmap)
            } else {
                GetProfilePicFromFirebaseAuth(activity, object : GetProfilePicFromFirebaseAuth.Callback {
                        override fun onComplete(bitmap: Bitmap?) {
                            val roundBitmap = RoundedBitmapDrawableFactory.create(activity.resources, bitmap)
                            roundBitmap.isCircular = true
                            holder.profilePic.setImageDrawable(roundBitmap)
                            DiskIOHelper.saveImageCache(activity, bitmap, s.userObj.uid)
                            Log.i("GymAdapter", "Caching Profile Pic for ${s.userObj.uid}")
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Uri.parse(s.userObj.profilePicUri)
                )
            }
    }

    /**
     * Creates the required View Holder to store the data in
     * @param parent ViewGroup The parent view (ignored)
     * @param viewType Int The type of view to create (ignored)
     * @return FavViewHolder The view holder that is created to store the data in
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_gym_rating, parent, false)
        return ReviewViewHolder(itemView)
    }

    /**
     * Inner class to store the review data in
     *
     * @author Kenneth Soh
     * @since 2019-10-13
     * @property name TextView User Name
     * @property date TextView User Review Date Posted
     * @property profilePic ImageView User Profile Picture
     * @property review TextView User Review
     * @property rating AppCompatRatingBar User Rating
     * @constructor Creates a view holder to hold the view required for RecyclerView
     */
    inner class ReviewViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView = v.findViewById(R.id.rate_name)
        var date: TextView = v.findViewById(R.id.rate_date)
        var profilePic: ImageView = v.findViewById(R.id.rate_pic)
        var review: TextView = v.findViewById(R.id.rate_review)
        var rating: AppCompatRatingBar = v.findViewById(R.id.rate_rating)

        init {
            v.tag = this
        }

    }

}