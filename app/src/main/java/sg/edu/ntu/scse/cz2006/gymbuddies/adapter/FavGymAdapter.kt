package sg.edu.ntu.scse.cz2006.gymbuddies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.cz2006.gymbuddies.R
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList
import java.util.*
import kotlin.collections.ArrayList

/**
 * Recycler Adapter for Favourited Gyms
 * For sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-02
 * @property gymList List<FavGymObject> The list of all gyms favourited by the user
 * @property onClickListener OnClickListener? Set this to override the default onClick listener defined in [FavGymAdapter.FavViewHolder.onClick]
 * @constructor Creates a adapter for the Gym Favourites List RecyclerView
 */
class FavGymAdapter(gyms: List<FavGymObject>) : RecyclerView.Adapter<FavGymAdapter.FavViewHolder>() {

    private var gymList: List<FavGymObject> = ArrayList()
    private var onClickListener: View.OnClickListener? = null

    init {
        this.gymList = gyms
    }

    /**
     * Overrides the default on click [listener]
     * @param listener OnClickListener Custom onclick listener. Set to null to revert back to default defined in [FavGymAdapter.FavViewHolder.onClick]
     */
    fun setOnClickListener(listener: View.OnClickListener) {
        onClickListener = listener
    }

    /**
     * Gets the number of gyms stored in this adapter
     * @return Int Number of gyms in this adapter
     */
    override fun getItemCount(): Int {
        return gymList.size
    }

    /**
     * Gets the gym list stored in this adapter
     * @return List<FavGymObject> Gym List object
     */
    fun getList(): List<FavGymObject> { return gymList }

    /**
     * Updates the gym list stored in this adapter
     * @param newList List<FavGymObject> New Gym List object
     */
    fun updateList(newList: List<FavGymObject>) { this.gymList = newList }

    /**
     * Internal function to bind the view [holder] at the current RecyclerView [position] to a specific data
     * @param holder FavViewHolder Holder to store the data
     * @param position Int Positing in the RecyclerView
     */
    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val s = gymList[position]
        holder.title.text = s.gym.properties.Name
        holder.favCount.text = "(${s.favCount})"
        holder.ratingAvg.text = "%.2f".format(Locale.US, s.avgRating)
        holder.rating.rating = s.avgRating
        holder.ratingCount.text = "(${s.ratingCount})"
        holder.gymObj = s.gym
    }

    /**
     * Creates the required View Holder to store the data in
     * @param parent ViewGroup The parent view (ignored)
     * @param viewType Int The type of view to create (ignored)
     * @return FavViewHolder The view holder that is created to store the data in
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_gym_detail, parent, false)
        return FavViewHolder(itemView, onClickListener)
    }

    /**
     * Inner class to store the favourited gym data in
     *
     * @author Kenneth Soh
     * @since 2019-09-17
     * @property title TextView Gym Title
     * @property rating AppCompatRatingBar Gym Rating
     * @property ratingCount TextView Number of ratings
     * @property ratingAvg TextView Average of the total ratings
     * @property favCount TextView Number of users who favourited this gym
     * @property gymObj GymShell? Gym object
     * @constructor Creates a view holder to hold the view required for RecyclerView
     */
    inner class FavViewHolder(v: View, listener: View.OnClickListener?) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var title: TextView = v.findViewById(R.id.fav_list_title)
        var rating: AppCompatRatingBar = v.findViewById(R.id.fav_list_rating)
        var ratingCount: TextView = v.findViewById(R.id.fav_list_rating_count)
        var ratingAvg: TextView = v.findViewById(R.id.fav_list_rating_avg)
        var favCount: TextView = v.findViewById(R.id.fav_list_favourites)
        var gymObj: GymList.GymShell? = null

        init {
            v.setOnClickListener(listener ?: this)
            v.tag = this
        }

        /**
         * Default onclick handler
         * @param p0 View View context object
         */
        override fun onClick(p0: View?) {
            p0?.let { Toast.makeText(it.context, title.text, Toast.LENGTH_SHORT).show() }
        }

    }

}