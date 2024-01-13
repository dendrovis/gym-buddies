package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdsearch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * View Model object for the Buddy Search Fragment
 * Note: This is not really being used for the Buddy Search Fragment, but its here due the MVVM model that Jetpack is being based off on
 *
 * @author Chia Yu
 * @since 2019-10-03
 */
public class BuddySearchViewModel extends ViewModel {
    /**
     * Sample Live Data
     */
    private MutableLiveData<String> mText;

    /**
     * Constructor for this view model
     */
    public BuddySearchViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is BD search fragment");
    }

    /**
     * Gets the sample text live data
     * @return Live Data of the sample
     */
    public LiveData<String> getText() {
        return mText;
    }
}