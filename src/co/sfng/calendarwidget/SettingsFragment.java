package co.sfng.calendarwidget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;


public class SettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.toString();
    private Resources mResources;
    private String mVersionName;
    private HashMap<String, String> mThemeReverseLookup;
    private HashMap<String, String> mWeekStartDayReverseLookup;
    private HashMap<String, String> mOnDayClickActionReverseLookup;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mVersionName = getVersionName(activity);
        mResources = getResources();

        mThemeReverseLookup = createReverseLookup(mResources, R.array.themes, R.array.theme_values);
        mWeekStartDayReverseLookup = createReverseLookup(
                mResources, R.array.week_start_day, R.array.week_start_day_values);
        mOnDayClickActionReverseLookup = createReverseLookup(
                mResources, R.array.on_day_click_actions, R.array.on_day_click_action_values);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Add listener.
        SharedPreferences sharedPref = getPreferenceScreen().getSharedPreferences();
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Read preferences and set as summary.
        PreferenceManager prefMgr = getPreferenceManager();
        Resources res = getResources();
        setSummary(prefMgr, res.getString(R.string.pref_version), mVersionName);
        setSummary(prefMgr, sharedPref, mThemeReverseLookup, res.getString(R.string.pref_theme));
        setSummary(prefMgr, sharedPref, mWeekStartDayReverseLookup,
                res.getString(R.string.pref_week_start_day));
        setSummary(prefMgr, sharedPref, mOnDayClickActionReverseLookup,
                res.getString(R.string.pref_on_day_click_action));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
        PreferenceManager prefMgr = getPreferenceManager();
        Preference pref = prefMgr.findPreference(key);
        assert(pref != null);
        String value = sharedPref.getString(key, null);
        assert(value != null);

        if (key.equals(mResources.getString(R.string.pref_theme))) {
            setSummary(prefMgr, sharedPref, mThemeReverseLookup, key);
        } else if (key.equals(mResources.getString(R.string.pref_week_start_day))) {
            setSummary(prefMgr, sharedPref, mWeekStartDayReverseLookup, key);
        } else if (key.equals(mResources.getString(R.string.pref_on_day_click_action))) {
            setSummary(prefMgr, sharedPref, mOnDayClickActionReverseLookup, key);
        }
    }

    private HashMap<String, String> createReverseLookup(Resources res, int keyArrayId,
            int valueArrayId) {
        // Assumption: Android retrieves the array in the order in which it was defined.
        HashMap<String, String> hashmap = new HashMap<String, String>();
        String[] keys = res.getStringArray(keyArrayId);
        String[] values = res.getStringArray(valueArrayId);
        for (int i = 0; i < keys.length; i++) {
            hashmap.put(values[i], keys[i]);
        }
        return hashmap;
    }

    private void setSummary(PreferenceManager prefMgr, String prefKey, String s) {
        Preference pref = prefMgr.findPreference(prefKey);
        pref.setSummary(s);
    }

    private void setSummary(PreferenceManager prefMgr, SharedPreferences sharedPref,
            HashMap<String, String> lookupHashMap, String prefKey) {
        String lookup = sharedPref.getString(prefKey, null);
        assert (lookup != null);
        setSummary(prefMgr, prefKey, lookupHashMap.get(lookup).toString());
    }

    private String getVersionName(Context context) {
        String versionName = "";
        String pkgName = context.getPackageName();
        try {
            versionName = context.getPackageManager().getPackageInfo(pkgName, 0).versionName;
        } catch(PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Name not found while getting application version name.", e);
        }
        return versionName;
    }
}
