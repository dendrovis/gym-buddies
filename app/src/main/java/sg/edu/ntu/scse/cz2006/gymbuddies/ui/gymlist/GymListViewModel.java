package sg.edu.ntu.scse.cz2006.gymbuddies.ui.gymlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;

import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.FavGymObject;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.GymList;

/**
 * View model for the favourite gyms fragment
 *
 * @author Kenneth Soh
 * @since 2019-10-03
 */
public class GymListViewModel extends ViewModel {

    /**
     * Live data for the selected gym object
     */
    private MutableLiveData<GymList.GymShell> selectedGym = new MutableLiveData<>(null);
    /**
     * Live data for the current selected gym object favourite count
     */
    private MutableLiveData<Integer> favCount = new MutableLiveData<>(0);
    /**
     * Live data of the current logged in user's favourite gyms
     */
    private MutableLiveData<HashMap<String, Integer>> currentUserFavList = new MutableLiveData<>(new HashMap<>());

    /**
     * Gets selected gym live data
     * @return Selected Gym Object
     */
    public LiveData<GymList.GymShell> getSelectedGym() { return selectedGym; }

    /**
     * Gets selected gym favourite count live data
     * @return Selected gym favourite count data
     */
    public LiveData<Integer> getFavCount() { return favCount; }

    /**
     * Updates selected gym
     * @param gym New selected gym
     */
    public void setSelectedGym(@Nullable FavGymObject gym) {
        if (gym == null) {
            this.selectedGym.setValue(null);
            this.favCount.setValue(0);
        }
        else {
            this.selectedGym.setValue(gym.getGym());
            this.favCount.setValue(gym.getFavCount());
        }
    }

    /**
     * Updates favourite count for selected gym
     * @param count New favourite count for selected gym
     */
    public void updateFavCount(int count) {
        this.favCount.setValue(count);
    }

    /**
     * Gets the live data of the user's current favourited gyms
     * @return Logged in user's favourited gym list (String - Gym ID, Integer - Favourites Count)
     */
    public LiveData<HashMap<String, Integer>> getCurrentUserFavourites() {
        return currentUserFavList;
    }

    /**
     * Sets the current logged in user's favourited gyms and their live favourite counts
     * @param currentFav Logged in user's favourited gym and favourite counts
     */
    public void updateCurrentUserFavourites(@NonNull HashMap<String, Integer> currentFav) {
        currentUserFavList.setValue(currentFav);
    }
}