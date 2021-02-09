package it.uniba.di.sms2021.managerapp.projects;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import java.util.HashMap;
import java.util.Map;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class ProjectPermissionsPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
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
        preferences.edit().putBoolean("visibility", group.getPermissions().isAccessible()).apply();

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("visibility"))  {
            boolean value = sharedPreferences.getBoolean(key, false);
            group.getPermissions().setAccessible(value);

            Map<String, Object> map = new HashMap<>();
            map.put("/permissions/", group.getPermissions());
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                    .child(group.getId()).updateChildren(map);
        }
    }
}
