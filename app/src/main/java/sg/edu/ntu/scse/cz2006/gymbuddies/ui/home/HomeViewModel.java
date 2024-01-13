package sg.edu.ntu.scse.cz2006.gymbuddies.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * View Model object for the Home Fragment
 * Note: This is not really being used for the Home Fragment, but its here due the MVVM model that Jetpack is being based off on
 *
 * @author Kenneth Soh
 * @since 2019-10-03
 */
public class HomeViewModel extends AndroidViewModel {

    /**
     * Sample Live Data
     */
    private MutableLiveData<String> mText;

    /**
     * Constructor for this view model
     * @param application Application object
     */
    public HomeViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    /**
     * Gets the sample text live data
     * @return Live Data of the sample
     */
    public LiveData<String> getText() {
        return mText;
    }
}