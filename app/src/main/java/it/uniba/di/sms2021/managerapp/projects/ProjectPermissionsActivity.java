package it.uniba.di.sms2021.managerapp.projects;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProjectPermissionsActivity extends AbstractBottomNavigationActivity implements View.OnClickListener {
    public static final String CREATION_BOOLEAN_KEY = "creation";

    private Project project;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsFragment, new ProjectPermissionsPreferencesFragment())
                .commit();

        doneButton = findViewById(R.id.done_button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        project = getIntent().getParcelableExtra(Project.KEY);
        if (getIntent().getBooleanExtra(CREATION_BOOLEAN_KEY, false)) {
            doneButton.setOnClickListener(this);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            doneButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_permissions;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            finish();
        } else {
            fragmentManager.popBackStack();
        }

        return super.onSupportNavigateUp();
    }

    public Project getProject() {
        return project;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.done_button) {
            finish();
        }
    }
}
