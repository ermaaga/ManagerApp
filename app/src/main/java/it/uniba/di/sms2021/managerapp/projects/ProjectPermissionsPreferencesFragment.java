package it.uniba.di.sms2021.managerapp.projects;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;

public class ProjectPermissionsPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String VISIBILITY_KEY = "visibility";
    private static final String JOINABLE_KEY = "free_join";
    private static final String MAX_MEMBERS_KEY = "max_members";

    private Group group;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_screen_project_permissions);
    }

    @Override
    public void onResume() {
        super.onResume();
        group = ((ProjectPermissionsActivity) getActivity()).getGroup();

        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        if (group.getPermissions() != null) {
            preferences.edit()
                    .putBoolean(VISIBILITY_KEY, group.getPermissions().isAccessible())
                    .putBoolean(JOINABLE_KEY, group.getPermissions().isJoinable())
                    .putString(MAX_MEMBERS_KEY, String.valueOf(group.getPermissions().getMaxMembers()))
                    .apply();
        }

        //Solo il creatore del gruppo può editare le autorizzazioni
        if (!group.getMembri().get(0).equals(LoginHelper.getCurrentUser().getAccountId())) {
            getPreferenceScreen().findPreference(VISIBILITY_KEY).setEnabled(false);
            getPreferenceScreen().findPreference(JOINABLE_KEY).setEnabled(false);
            getPreferenceScreen().findPreference(MAX_MEMBERS_KEY).setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(VISIBILITY_KEY))  {
            boolean value = sharedPreferences.getBoolean(key, false);
            group.getPermissions().setAccessible(value);
        } else if (key.equals(JOINABLE_KEY)) {
            boolean value = sharedPreferences.getBoolean(key, true);
            group.getPermissions().setJoinable(value);
        } else if (key.equals(MAX_MEMBERS_KEY)) {
            int value = Integer.parseInt(sharedPreferences.getString(key, "0"));
            if (value != 0 && value - group.getMembri().size() < 0) {
                Toast.makeText(getContext(), R.string.text_message_max_members_exceeds_actual,
                        Toast.LENGTH_LONG).show();
            }

            group.getPermissions().setMaxMembers(value);
        } else {
            throw new RuntimeException("Preference key not found");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("/permissions/", group.getPermissions());
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                .child(group.getId()).updateChildren(map);
    }
}
