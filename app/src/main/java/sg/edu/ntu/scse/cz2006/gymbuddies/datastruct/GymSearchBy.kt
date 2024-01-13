package sg.edu.ntu.scse.cz2006.gymbuddies.datastruct

/**
 * Data Structure to store Gym Search Parameters
 *
 * Note that in rating, -1 = All, 1-5 is the respective counts
 * @property sort Int Sort val, MUST be [SORT_ABC_ASC], [SORT_ABC_DSC], [SORT_FAV_ASC], [SORT_FAV_DSC]
 * @property distance Double Distance radius around the user
 * @property rating Int rating selected, will be that rating and greater. -1 for all
 * @constructor Gym Search Parameters
 */
data class GymSearchBy(val sort: Int = SORT_ABC_ASC, val distance: Double = 10.0, val rating: Int = -1) {
    companion object {
        /**
         * Sort gyms by alphabetical ascending order
         */
        const val SORT_ABC_ASC = 0
        /**
         * Sort gyms by alphabetical descending order
         */
        const val SORT_ABC_DSC = 1
        /**
         * Sort gyms by popularity ascending order
         */
        const val SORT_FAV_ASC = 2
        /**
         * Sort gyms by popularity descending order
         */
        const val SORT_FAV_DSC = 3
    }
}