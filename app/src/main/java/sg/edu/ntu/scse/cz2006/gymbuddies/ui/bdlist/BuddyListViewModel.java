package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * View Model object for the BuddyList Fragment
 * Note: This is not really being used for the BuddyList Fragment, but its here due the MVVM model that Jetpack is being based off on
 *
 * @author Chia Yu
 * @since 2019-10-03
 */
public class BuddyListViewModel extends ViewModel {
    /**
     * Sample Live Data
     */
    private MutableLiveData<String> mText;

    /**
     * Constructor for this view model
     */
    public BuddyListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is BD List");
    }

    /**
     * Gets the sample text live data
     * @return Live Data of the sample
     */
    public LiveData<String> getText() {
        return mText;
    }
}