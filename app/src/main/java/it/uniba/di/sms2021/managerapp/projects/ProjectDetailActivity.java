package it.uniba.di.sms2021.managerapp.projects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.db.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProjectDetailActivity extends AbstractTabbedNavigationHubActivity {
    private static final int NOTICES_TAB_POSITION = 0;
    private static final int FILES_TAB_POSITION = 1;
    private static final int MEMBERS_TAB_POSITION = 2;

    private static final String TAG = "ProjectDetailActivity";

    private Project project;

    @Override
    protected Fragment getInitialFragment() {
        return new ProjectNoticesFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        project = getIntent().getParcelableExtra(Project.KEY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(project.getName() + " - " + project.getStudyCaseName());
        actionBar.setSubtitle(project.getExamName());
        Log.i(TAG, project.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_project_detail;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_projects;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPosition = tab.getPosition();
        if (tabPosition == NOTICES_TAB_POSITION) {
            navigateTo(new ProjectNoticesFragment(), false);
        } else if (tabPosition == FILES_TAB_POSITION) {
            navigateTo(new ProjectFilesFragment(), false);
        } else if (tabPosition == MEMBERS_TAB_POSITION) {
            navigateTo(new ProjectMembersFragment(), false);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

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

    public Project getSelectedProject () {
        return project;
    }
}