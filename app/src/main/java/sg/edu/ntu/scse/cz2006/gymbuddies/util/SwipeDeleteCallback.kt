package sg.edu.ntu.scse.cz2006.gymbuddies.util

import android.content.Context
import android.graphics.*
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.cz2006.gymbuddies.R

/**
 * Helper class for handling RecyclerView swipe capabilities. Calls the [callback] when swiped
 * For sg.edu.ntu.scse.cz2006.gymbuddies.util in Gym Buddies!
 *
 * @author Kenneth Soh
 * @since 2019-10-03
 * @property callback ISwipeCallback The callback used when a valid swipe is detected
 * @property context Context Application Context
 * @property swipeDirection Int The direction to swipe. Can be ItemTouchHelper.LEFT, ItemTouchHelper.RIGHT or SWIPE_ANY
 * @property removeIcon Int? A custom delete icon if you wish to implement. Requires a drawable id
 * @property deleteBackground Int The background color for delete. Set to [Color.RED]
 * @property iconColor Paint The icon color, set to [Color.WHITE]
 * @property deleteBitmap Bitmap The delete bitmap to draw. Can pass in your own through [removeIcon]
 * @constructor Generates a callback that will be invoked when a user swipes on the selected recycler view
 */
class SwipeDeleteCallback(var callback: ISwipeCallback, var context: Context, private var swipeDirection: Int = ItemTouchHelper.LEFT, @DrawableRes var removeIcon: Int? = null) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val deleteBackground: Int = Color.RED
    private val iconColor: Paint = Paint().apply { color = Color.WHITE; colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
    private val deleteBitmap: Bitmap = ProfilePicHelper.getBitmap(context, removeIcon ?: R.drawable.ic_delete)
    init { if (swipeDirection != SWIPE_ANY) setDefaultSwipeDirs(swipeDirection) }

    /**
     * Called when ItemTouchHelper wants to move the dragged item from its old position to
     * the new position.
     *
     * If this method returns true, ItemTouchHelper assumes [viewHolder] has been moved
     * to the adapter position of [target] ViewHolder
     * ({@link ViewHolder#getAdapterPosition()
     * ViewHolder#getAdapterPosition()}).
     *
     * If you don't support drag & drop, this method will never be called.
     *
     * @param recyclerView The RecyclerView to which ItemTouchHelper is attached to.
     * @param viewHolder   The ViewHolder which is being dragged by the user.
     * @param target       The ViewHolder over which the currently active item is being
     *                     dragged.
     * @return True if the [viewHolder] has been moved to the adapter position of
     * {@code target}.
     * @see #onMoved(RecyclerView, ViewHolder, int, ViewHolder, int, int, int)
     */
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }

    /**
     * Called when a ViewHolder is swiped by the user.
     *
     * If you are returning relative directions ({@link #START} , {@link #END}) from the
     * {@link #getMovementFlags(RecyclerView, ViewHolder)} method, this method
     * will also use relative directions. Otherwise, it will use absolute directions.
     *
     * If you don't support swiping, this method will never be called.
     *
     * ItemTouchHelper will keep a reference to the View until it is detached from
     * RecyclerView.
     * As soon as it is detached, ItemTouchHelper will call
     * {@link #clearView(RecyclerView, ViewHolder)}.
     *
     * @param viewHolder The ViewHolder which has been swiped by the user.
     * @param direction  The direction to which the ViewHolder is swiped. It is one of
     *                   {@link #UP}, {@link #DOWN},
     *                   {@link #LEFT} or {@link #RIGHT}. If your
     *                   {@link #getMovementFlags(RecyclerView, ViewHolder)}
     *                   method
     *                   returned relative flags instead of {@link #LEFT} / {@link #RIGHT};
     *                   `direction` will be relative as well. ({@link #START} or {@link
     *                   #END}).
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == swipeDirection || swipeDirection == SWIPE_ANY) callback.delete(viewHolder.adapterPosition)
    }

    /**
     * Called by ItemTouchHelper on RecyclerView's onDraw callback.
     *
     * If you would like to customize how your View's respond to user interactions, this is
     * a good place to override.
     *
     * Default implementation translates the child by the given [dX], [dY].
     * ItemTouchHelper also takes care of drawing the child after other children if it is being
     * dragged. This is done using child re-ordering mechanism. On platforms prior to L, this
     * is
     * achieved via {@link android.view.ViewGroup#getChildDrawingOrder(int, int)} and on L
     * and after, it changes View's elevation value to be greater than all other children.)
     *
     * @param c                 The canvas which RecyclerView is drawing its children
     * @param recyclerView      The RecyclerView to which ItemTouchHelper is attached to
     * @param viewHolder        The ViewHolder which is being interacted by the User or it was
     *                          interacted and simply animating to its original position
     * @param dX                The amount of horizontal displacement caused by user's action
     * @param dY                The amount of vertical displacement caused by user's action
     * @param actionState       The type of interaction on the View. Is either {@link
     *                          #ACTION_STATE_DRAG} or {@link #ACTION_STATE_SWIPE}.
     * @param isCurrentlyActive True if this view is currently being controlled by the user or
     *                          false it is simply animating back to its original state.
     * @see #onChildDrawOver(Canvas, RecyclerView, ViewHolder, float, float, int, boolean)
     */
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            val swipeDirection = if (dX > 0) ItemTouchHelper.RIGHT else if (dX < 0) ItemTouchHelper.LEFT else ItemTouchHelper.UP
            val selectedBackground = deleteBackground
            val selectedBitmap = deleteBitmap

            val height = itemView.bottom.toFloat() - itemView.top.toFloat()
            val width = height / 3
            val p = Paint().apply { color = selectedBackground }

            when (swipeDirection) {
                ItemTouchHelper.RIGHT -> {
                    val bg = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                    c.drawRect(bg, p)
                    val iconDest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(selectedBitmap, null, iconDest, iconColor)
                }
                ItemTouchHelper.LEFT -> {
                    val bg = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    c.drawRect(bg, p)
                    val iconDest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(selectedBitmap, null, iconDest, iconColor)
                }
            }
        }
    }

    companion object {
        /**
         * Allows swiping from any direction
         */
        const val SWIPE_ANY=-2
    }

    /**
     * Interface that other classes implement to handle swipe callback
     */
    interface ISwipeCallback {
        /**
         * Successfully deleted at a specific position on the RecyclerView
         * @param position Int? Position that the item was deleted from
         * @return Boolean true if successful, false otherwise (if false, assumed failed and not continue removal)
         */
        fun delete(position: Int?): Boolean
    }
}