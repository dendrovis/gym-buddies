package sg.edu.ntu.scse.cz2006.gymbuddies.ui.bdsearch;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import sg.edu.ntu.scse.cz2006.gymbuddies.BuddySearchResultActivity;
import sg.edu.ntu.scse.cz2006.gymbuddies.R;


/**
 * The fragment display search options for gym buddies
 * @author Chia Yu
 * @since 2019-09-06
 */
public class BuddySearchFragment extends Fragment {
    /**
     * view model to separate data from activity life cycle
     */
    private BuddySearchViewModel buddySearchViewModel;
    /**
     * View reference to search button
     */
    private Button btnSearch;
    /**
     * view reference to drop down list of location
     */
    private Spinner spPrefLocation;
    /**
     * view reference to radio group of gender
     */
    private RadioGroup rgBuddyGender;
    /**
     * view reference to radio group of preferred workout time
     */
    private RadioGroup rgPrefTime;
    /**
     * view reference to view group of preferred workout days
     */
    private LinearLayout llPrefDays;


    /**
     * Android framework lifecycle
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        buddySearchViewModel = ViewModelProviders.of(this).get(BuddySearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_buddy_search, container, false);
//        final TextView textView = root.findViewById(R.id.text_share);
        buddySearchViewModel.getText().observe(this, s -> {
//                textView.setText(s);
        });


        spPrefLocation = root.findViewById(R.id.spinner_live_region);
        rgPrefTime    = root.findViewById(R.id.rg_bd_time);
        rgBuddyGender = root.findViewById(R.id.rg_bd_gender);
        llPrefDays = root.findViewById(R.id.ll_pref_days);
        btnSearch = root.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), BuddySearchResultActivity.class);
            Bundle data = new Bundle();
            data.putString("pref_location", spPrefLocation.getSelectedItem().toString());
            data.putIntArray("pref_days", getPrefDays());
            data.putString("pref_time", getRadioText(rgPrefTime));
            data.putString("gender",  getRadioText(rgBuddyGender) );
            intent.putExtras(data);
            startActivity(intent);
        });

        return root;
    }

    /**
     * helper method to retrieve selecte radio button text base on radio group provided
     * @param rg
     * @return
     */
    private String getRadioText(RadioGroup rg){
        RadioButton selected = getView().findViewById(rg.getCheckedRadioButtonId());
        return (String) selected.getText();
    }

    /**
     * convert preferred workout days to int[],
     * where index denote the days, and value(0/1) denote the selection of the day
     * @return
     */
    private int[] getPrefDays(){
        int[] arPrefDays = new int[7];
        for (int i = 0; i<7; i++){
            if (((CheckBox)llPrefDays.getChildAt(i)).isChecked()){
                arPrefDays[i] = 1;
            }
        }
        return arPrefDays;
    }
}