package sg.edu.ntu.scse.cz2006.gymbuddies.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.cz2006.gymbuddies.R

/**
 * Recycler Adapter for Strings of text
 * For sg.edu.ntu.scse.cz2006.gymbuddies.adapter in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-09-17
 * @property announce Boolean Whether to display a toast message in onClick or not
 * @property stringList List<String> The list of all strings to display
 * @property onClickListener OnClickListener? Set this to override the default onClick listener defined in [StringRecyclerAdapter.StringViewHolder.onClick]
 * @constructor Creates a adapter for the Gym Favourites List RecyclerView
 */
class StringRecyclerAdapter(string: List<String>, private var announce: Boolean) : RecyclerView.Adapter<StringRecyclerAdapter.StringViewHolder>() {
    /**
     * Initializes the recycler adapter with just a list of string.
     * We will presume that you are implicitly wanting announcement when [StringRecyclerAdapter.StringViewHolder.onClick] is called
     * @param string List<String> The list of all strings to display
     * @constructor Creates a adapter for the Gym Favourites List RecyclerView
     */
    constructor(string: List<String>) : this(string, true)

    private var stringList: List<String> = ArrayList()
    private var onClickListener: View.OnClickListener? = null

    init { stringList = string }

    /**
     * Updates internal string list
     * @param newString List<String> New list of string
     */
    fun updateStrings(newString: List<String>) { stringList = newString }

    /**
     * Overrides the default on click [listener]
     * @param listener OnClickListener Custom onclick listener. Set to null to revert back to default defined in [StringRecyclerAdapter.StringViewHolder.onClick]
     */
    fun setOnClickListener(listener: View.OnClickListener) {
        onClickListener = listener
    }

    /**
     * Gets the number of strings stored in this adapter
     * @return Int Number of strings in this adapter
     */
    override fun getItemCount(): Int {
        return stringList.size
    }

    /**
     * Internal function to bind the view [holder] at the current RecyclerView [position] to a specific data
     * @param holder FavViewHolder Holder to store the data
     * @param position Int Positing in the RecyclerView
     */
    override fun onBindViewHolder(holder: StringViewHolder, position: Int) {
        val s = stringList[position]
        holder.title.text = s
    }

    /**
     * Creates the required View Holder to store the data in
     * @param parent ViewGroup The parent view (ignored)
     * @param viewType Int The type of view to create (ignored)
     * @return FavViewHolder The view holder that is created to store the data in
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_simple_list_item_1, parent, false)
        return StringViewHolder(itemView, onClickListener)
    }

    /**
     * Inner class to store the string data in
     *
     * @author Kenneth Soh
     * @since 2019-09-17
     * @property title TextView Main text
     * @constructor Creates a view holder to hold the view required for RecyclerView
     */
    inner class StringViewHolder(v: View, listener: View.OnClickListener?) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var title: TextView = v.findViewById(android.R.id.text1)

        init {
            v.setOnClickListener(listener ?: this)
            v.tag = this
        }

        /**
         * Default onclick handler
         * @param p0 View View context object
         */
        override fun onClick(p0: View?) {
            if (announce) p0?.let { Toast.makeText(it.context, title.text, Toast.LENGTH_SHORT).show() }
        }

    }

}