package it.uniba.di.sms2021.managerapp.exams;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ExamDetailActivity extends AbstractTabbedNavigationHubActivity {
    private static final int STUDY_CASES_TAB_POSITION = 0;
    private static final int GROUPS_TAB_POSITION = 1;

    @Override
    protected Fragment getInitialFragment() {
        return new ExamStudyCasesFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_exam_detail;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPosition = tab.getPosition();
        if (tabPosition == STUDY_CASES_TAB_POSITION) {
            navigateTo(new ExamStudyCasesFragment(), false);
        } else if (tabPosition == GROUPS_TAB_POSITION) {
            navigateTo(new ExamGroupsFragment(), false);
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

    public void scegliTemplate(View view) {
        startActivity(new Intent(this, NewGroupActivity.class));
    }
}