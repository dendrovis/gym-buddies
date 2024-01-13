package sg.edu.ntu.scse.cz2006.gymbuddies.util;


import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import org.jetbrains.annotations.Nullable;

import sg.edu.ntu.scse.cz2006.gymbuddies.R;
import sg.edu.ntu.scse.cz2006.gymbuddies.datastruct.User;
import sg.edu.ntu.scse.cz2006.gymbuddies.tasks.GetProfilePicFromFirebaseAuth;

/**
 * Utility class to assist repeated view update procedures
 *
 * @author Chia Yu
 * @since 2019-10-23
 */
public class ViewHelper {

    /**
     * update profile picture for each user
     * @see GetProfilePicFromFirebaseAuth
     * @see DiskIOHelper
     */
    public static void updateUserPic(ImageView imgView, User user){
        if (!user.getProfilePicUri().equals(imgView.getTag())) {
            imgView.setImageResource(R.mipmap.ic_launcher);
        }

        if (DiskIOHelper.hasImageCache(imgView.getContext(), user.getUid())){
            Bitmap bitmap = DiskIOHelper.readImageCache(imgView.getContext(), user.getUid());
            if (bitmap != null) {
                RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(imgView.getContext().getResources(), bitmap);
                roundBitmap.setCircular(true);
                imgView.setImageDrawable(roundBitmap);
                imgView.setTag(user.getProfilePicUri());
            }
        }else {
            if (!user.getProfilePicUri().isEmpty() && !user.getProfilePicUri().equalsIgnoreCase("null")) {
                Activity activity = (Activity) imgView.getContext();
                new GetProfilePicFromFirebaseAuth(activity, new GetProfilePicFromFirebaseAuth.Callback() {
                    @Override
                    public void onComplete(@Nullable Bitmap bitmap) {
                        if (bitmap != null) {
                            DiskIOHelper.saveImageCache(imgView.getContext(), bitmap, user.getUid());
                            RoundedBitmapDrawable roundBitmap = RoundedBitmapDrawableFactory.create(activity.getResources(), bitmap);
                            roundBitmap.setCircular(true);
                            imgView.setImageDrawable(roundBitmap);
                            imgView.setTag(user.getProfilePicUri());
                        }
                    }
                }).execute(Uri.parse(user.getProfilePicUri()));
            }
        }
    }
}
