package app.coolweather.develop.tlabs.com.coolweather;

import android.content.Intent;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_temprature_key)));
    }

    private void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String strigValue=(String) newValue;

        if(preference instanceof ListPreference){
            ListPreference listPreference= (ListPreference) preference;
            int prefIndex=listPreference.findIndexOfValue(strigValue);
            if(prefIndex>=0){
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }else{
            preference.setSummary(strigValue);
        }
        return true;
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
