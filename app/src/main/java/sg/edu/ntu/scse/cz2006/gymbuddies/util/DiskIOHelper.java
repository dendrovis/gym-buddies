package sg.edu.ntu.scse.cz2006.gymbuddies.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import sg.edu.ntu.scse.cz2006.gymbuddies.AppConstants;


/**
 * DiskIOHelper provides the ability to cache bitmap into application's internal storage.
 * it improves loading speed and reduce number of downloads to save user's data usage
 *
 * @author Chia Yu
 * @since 2019-10-23
 */
public class DiskIOHelper implements AppConstants {
    private static final String TAG = "gb.helper.IO";
    private static final String DIR_PIC = "pic";


    /**
     * the method checks whether cached bitmap file is exist in application's internal folder.
     * note that the bitmap only valid for a day, after that the cache will be deleted.
     * @param context
     * @param fileName user id will be more preferable
     * @return
     */
    public static boolean hasImageCache(Context context, String fileName){
        // check if file exist
        File cacheDir = new File(context.getCacheDir(), DIR_PIC);
        if (!cacheDir.exists()){
            Log.d(TAG, cacheDir.getAbsolutePath()+" -> not exist");
            return false;
        }
        File cacheFile = new File(cacheDir, fileName+".png");
        if (!cacheFile.exists()){
            Log.d(TAG, cacheFile.getAbsolutePath()+" -> not exist");
            return false;
        }

        // file is exist, validation cacheFile.lastModified()
        long now = System.currentTimeMillis();
        long diff = now - cacheFile.lastModified();
        if (diff > MAX_CACHE_DURATION){
            // remove cache file
            if (cacheFile.delete()){
                Log.d(TAG, cacheFile.getName()+" is deleted");
            }
            return false;
        }
        return true;
    }

    /**
     * the method perform saving of bitmap into application's internal storage
     * @param context
     * @param bmp
     * @param name
     */
    public static void saveImageCache(Context context, Bitmap bmp, String name){
        Log.d(TAG, "saving");
        File cacheDir = new File(context.getCacheDir(), DIR_PIC);
        if (!cacheDir.exists()){
            cacheDir.mkdirs();
            Log.d(TAG, cacheDir.getAbsolutePath()+" -> created");
        }

        FileOutputStream fos=null;
        File file = new File(cacheDir, name+".png");

        try{
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, fos);
            Log.d(TAG, "saved");
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            try {
                if (fos != null) {
                    Log.d(TAG, "close fos");
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * the method retrieve bitmap from application's internal storage
     * @param context
     * @param name
     * @return
     */
    public static Bitmap readImageCache(Context context, String name){
        File cacheDir = new File(context.getCacheDir(), DIR_PIC);
        File file = new File(cacheDir, name+".png");
        if (!file.exists()){
            Log.d(TAG, file.getAbsolutePath()+" is not found");
            return null;
        }

        FileInputStream fis = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream( fis, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis!=null){
                try{
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }






}
