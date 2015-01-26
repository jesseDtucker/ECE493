package jetucker.cmput293assignment1;

import android.content.Context;
import android.content.SharedPreferences;

public final class Settings
{
    private static final String SHARED_PREF = "filterPreferences";
    private static final String MEAN_SIZE_PREF = "meanFilterSize";
    private static final String MEDIAN_SIZE_PREF = "medianFilterSize";
    private static final int DEFAULT_FILTER_SIZE = 5;

    public static int GetMedianFilterSize(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF, 0);
        return prefs.getInt(MEDIAN_SIZE_PREF, DEFAULT_FILTER_SIZE);
    }

    public static void SetMedianFilterSize(Context context, int newValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF, 0);
        boolean success = prefs.edit().putInt(MEDIAN_SIZE_PREF, newValue).commit();
        Util.Assert(success);
    }

    public static int GetMeanFilterSize(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF, 0);
        return prefs.getInt(MEAN_SIZE_PREF, DEFAULT_FILTER_SIZE);
    }

    public static void SetMeanFilterSize(Context context, int newValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF, 0);
        boolean success = prefs.edit().putInt(MEAN_SIZE_PREF, newValue).commit();
        Util.Assert(success);
    }
}
