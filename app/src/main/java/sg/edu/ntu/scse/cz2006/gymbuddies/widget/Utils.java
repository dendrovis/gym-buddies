package sg.edu.ntu.scse.cz2006.gymbuddies.widget;

/**
 * Animated "heart" button based off Twitter's implementation
 * Adapted from https://github.com/frogermcs/LikeAnimation
 *
 * for sg.edu.ntu.scse.cz2006.gymbuddies.widget in Gym Buddies!
 *
 * @author Kenneth Soh, frogermcs
 * @since 2019-10-01
 */
public class Utils {
    public static double mapValueFromRangeToRange(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
        return toLow + ((value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow));
    }

    public static double clamp(double value, double low, double high) {
        return Math.min(Math.max(value, low), high);
    }
}
