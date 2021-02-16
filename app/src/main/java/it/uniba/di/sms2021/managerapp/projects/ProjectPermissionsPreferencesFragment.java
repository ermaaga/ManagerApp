package it.uniba.di.sms2021.managerapp.projects;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.UserSelectionRecyclerAdapter;

public class ProjectPermissionsPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String VISIBILITY_KEY = "visibility";
    private static final String JOINABLE_KEY = "free_join";
    private static final String MAX_MEMBERS_KEY = "max_members";
    private static final String FILE_ACCESSIBILITY_KEY = "file_accessibility";
    public static final String CAN_ADD_FILES_KEY = "can_add_files";

    private Project project;

    private List<String> selectedMembers;
    private UserSelectionRecyclerAdapter adapter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_screen_project_permissions);
    }

    @Override
    public void onResume() {
        super.onResume();
        project = ((ProjectPermissionsActivity) getActivity()).getProject();

        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        if (project.getPermissions() != null) {
            preferences.edit()
                    .putBoolean(VISIBILITY_KEY, project.getPermissions().isAccessible())
                    .putBoolean(JOINABLE_KEY, project.getPermissions().isJoinable())
                    .putString(MAX_MEMBERS_KEY, String.valueOf(project.getPermissions().getMaxMembers()))
                    .putBoolean(FILE_ACCESSIBILITY_KEY, project.getPermissions().isFileAccessible())
                    .apply();

            selectedMembers = new ArrayList<>();
            selectedMembers.addAll(project.getPermissions().getCanAddFiles());

            if (selectedMembers.size() == 0) {
                selectedMembers.add(project.getMembri().get(0));
            }
        }

        //Solo il creatore del gruppo può editare le autorizzazioni
        if (!project.getMembri().get(0).equals(LoginHelper.getCurrentUser().getAccountId())) {
            getPreferenceScreen().findPreference(VISIBILITY_KEY).setEnabled(false);
            getPreferenceScreen().findPreference(JOINABLE_KEY).setEnabled(false);
            getPreferenceScreen().findPreference(MAX_MEMBERS_KEY).setEnabled(false);
            getPreferenceScreen().findPreference(FILE_ACCESSIBILITY_KEY).setEnabled(false);
            getPreferenceScreen().findPreference(CAN_ADD_FILES_KEY).setEnabled(false);
        }

        Preference canAddFilesPreference = getPreferenceScreen().findPreference(CAN_ADD_FILES_KEY);
        canAddFilesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<String> addableMembers = project.getMembri()
                                        .subList(1, project.getMembri().size());
                                List<User> members = new ArrayList<>();

                                for (DataSnapshot child: snapshot.getChildren()) {
                                    if (addableMembers.contains(child.getKey())) {
                                        members.add(child.getValue(User.class));
                                    }
                                }

                                adapter.submitList(members);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                adapter = new UserSelectionRecyclerAdapter(getContext(),
                        new UserSelectionRecyclerAdapter.OnActionListener() {
                            @Override
                            public void onItemClicked() {

                            }
                        }, selectedMembers);
                RecyclerView recyclerView = new RecyclerView(requireContext());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.text_label_project_preference_can_add_file)
                        .setView(recyclerView)
                        .setPositiveButton(R.string.text_button_confirm,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d("TEST", adapter.getSelectedItems().toString());
                                        selectedMembers.clear();
                                        selectedMembers.add(project.getMembri().get(0));

                                        for (User user: adapter.getSelectedItems()) {
                                            selectedMembers.add(user.getAccountId());
                                        }
                                        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(),
                                                CAN_ADD_FILES_KEY);
                                    }
                                })
                        .setNegativeButton(R.string.text_button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();
                return false;
            }
        });
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
            project.getPermissions().setAccessible(value);
        } else if (key.equals(JOINABLE_KEY)) {
            boolean value = sharedPreferences.getBoolean(key, true);
            project.getPermissions().setJoinable(value);
        } else if (key.equals(MAX_MEMBERS_KEY)) {
            int value = Integer.parseInt(sharedPreferences.getString(key, "0"));
            if (value != 0 && value - project.getMembri().size() < 0) {
                Toast.makeText(getContext(), R.string.text_message_max_members_exceeds_actual,
                        Toast.LENGTH_LONG).show();
                return;
            }

            project.getPermissions().setMaxMembers(value);
        } else if (key.equals(FILE_ACCESSIBILITY_KEY)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            project.getPermissions().setFileAccessible(value);
        } else if (key.equals(CAN_ADD_FILES_KEY)) {
            Log.d("TEST", selectedMembers.toString());
            project.getPermissions().setCanAddFiles(selectedMembers);
        }
        else {
            throw new RuntimeException("Preference key not found");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("/permissions/", project.getPermissions());
        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                .child(project.getId()).updateChildren(map);
    }
}
