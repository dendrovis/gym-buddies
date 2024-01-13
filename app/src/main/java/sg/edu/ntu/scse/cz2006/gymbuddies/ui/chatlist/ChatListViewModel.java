package sg.edu.ntu.scse.cz2006.gymbuddies.ui.chatlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * View Model object for the Chat List Fragment
 * Note: This is not really being used for the Chat List Fragment, but its here due the MVVM model that Jetpack is being based off on
 *
 * @author Chia Yu
 * @since 2019-10-03
 */
public class ChatListViewModel extends ViewModel {
    /**
     * Sample Live Data
     */
    private MutableLiveData<String> mText;

    /**
     * Constructor for this view model
     */
    public ChatListViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Chat fragment");
    }

    /**
     * Gets the sample text live data
     * @return Live Data of the sample
     */
    public LiveData<String> getText() {
        return mText;
    }
}