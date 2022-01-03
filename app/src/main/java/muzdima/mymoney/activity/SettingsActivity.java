package muzdima.mymoney.activity;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import muzdima.mymoney.R;
import muzdima.mymoney.utils.ConfigurationPreferences;

public class SettingsActivity extends BaseActivity {

    private boolean firstResume = true;

    @Override
    protected String getMenuTitle() {
        return getString(R.string.settings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsFragment fragment = new SettingsFragment();
        fragment.activity = this;
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .disallowAddToBackStack()
                .commit();
    }

    @Override
    public void onResume() {
        if (firstResume) {
            firstResume = false;
            super.onResume();
            return;
        }
        super.onResume();
        recreate();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        public SettingsActivity activity;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference, rootKey);
            Preference.OnPreferenceChangeListener recreateAction = (preference, newValue) -> {
                if (activity != null) {
                    activity.runOnUiThread(activity::recreate);
                }
                return true;
            };
            Preference.OnPreferenceChangeListener updateAction = (preference, newValue) -> {
                if (activity != null) {
                    activity.runOnUiThread(()-> ConfigurationPreferences.updateFormatPreferences(activity, preference.getKey(), (String) newValue));
                }
                return true;
            };
            Objects.requireNonNull((Preference)findPreference(getString(R.string.preference_language))).setOnPreferenceChangeListener(recreateAction);
            Objects.requireNonNull((Preference)findPreference(getString(R.string.preference_night_mode))).setOnPreferenceChangeListener(recreateAction);
            Objects.requireNonNull((Preference)findPreference(getString(R.string.preference_format_number_decimal))).setOnPreferenceChangeListener(updateAction);
            Objects.requireNonNull((Preference)findPreference(getString(R.string.preference_format_number_thousand))).setOnPreferenceChangeListener(updateAction);
            Objects.requireNonNull((Preference)findPreference(getString(R.string.preference_format_date))).setOnPreferenceChangeListener(updateAction);
            Objects.requireNonNull((Preference)findPreference(getString(R.string.preference_format_time))).setOnPreferenceChangeListener(updateAction);
        }
    }
}
