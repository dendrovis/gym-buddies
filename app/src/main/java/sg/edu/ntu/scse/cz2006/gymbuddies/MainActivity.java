package sg.edu.ntu.scse.cz2006.gymbuddies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.DiskIOHelper;
import sg.edu.ntu.scse.cz2006.gymbuddies.util.ProfilePicHelper;

/**
 * The application's main activity
 * This activity will contain data regarding the various fragments and is mainly used for Jetpack
 *
 * @author Chiayu, Kenneth
 * @since 2019-09-06
 */
public class MainActivity extends AppCompatActivity {

    /**
     * The app bar configuration object
     */
    private AppBarConfiguration mAppBarConfiguration;

    /**
     * Internal lifecycle function when this activity is created
     * @param savedInstanceState Saved instance state for configuration changes
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gym_list, R.id.nav_bd_search,  R.id.nav_bd_list, R.id.nav_chat_list)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Check if user is supposed to be here
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            logout(); // No user found
            return;
        }
        // Check email validated for email
        firebaseUser.getProviderData();
        for (UserInfo provider : firebaseUser.getProviderData()) {
            if (!provider.getProviderId().equalsIgnoreCase("password")) continue;
            if (!firebaseUser.isEmailVerified()) {
                logout(); // Email Authentication and user not verified
                return;
            }
        }

        // Set user name and email
        View header = navigationView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.email)).setText(firebaseUser.getEmail());
        ((TextView) header.findViewById(R.id.name)).setText(firebaseUser.getDisplayName());
        if (firebaseUser.getPhotoUrl() != null && !firebaseUser.getPhotoUrl().toString().equalsIgnoreCase("null"))
            new GetProfilePicFromFirebaseAuth(this, bitmap -> { if (bitmap != null) {
                RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundBitmap.setCircular(true);
                ProfilePicHelper.setProfilePic(roundBitmap);
                DiskIOHelper.saveImageCache(this, bitmap, firebaseUser.getUid());
                Log.i("Main", "Updated your own profile picture");
                ((ImageView) header.findViewById(R.id.profile_pic)).setImageDrawable(roundBitmap);
            } }).execute(firebaseUser.getPhotoUrl()); // Download and set as profile pic
        header.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileEditActivity.class);
            i.putExtra("view", true);
            startActivity(i);
        });

        Menu navMenu = navigationView.getMenu();
        navMenu.findItem(R.id.nav_logout).setOnMenuItemClickListener(menuItem -> { logout(); return false; });
        navMenu.findItem(R.id.nav_settings).setOnMenuItemClickListener(menuItem -> { startActivity(new Intent(this, SettingsActivity.class)); return false; });
    }

    /**
     * Logs a user out of the application
     */
    private void logout() {
        Intent logout = new Intent(this, LoginChooserActivity.class);
        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        logout.putExtra("logout", true);
        startActivity(logout);
        finish();
    }

    /**
     * Internal function to override if the activity supports going back
     * @return true if supported, false otherwise
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    /**
     * Internal lifecycle function for if we are granted a permission
     * We use this for the location permission
     *
     * @param requestCode The request code for the permission for reference
     * @param permissions The permissions being requested
     * @param grantResults The results of the permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
