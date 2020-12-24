package it.uniba.di.sms2021.managerapp.home;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;

import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class HomeActivity extends AbstractTabbedNavigationHubActivity {
    private static final int SOCIAL_IDEAS_TAB_POSITION = 0;
    private static final int PROJECT_TAB_POSITION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (fragmentManager.getBackStackEntryCount() != 0) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        });
    }

    @Override
    protected Fragment getInitialFragment() {
        return new HomeSocialIdeasFragment();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPosition = tab.getPosition();
        if (tabPosition == SOCIAL_IDEAS_TAB_POSITION) {
            navigateTo(new HomeSocialIdeasFragment(), false);
        } else if (tabPosition == PROJECT_TAB_POSITION) {
            navigateTo(new HomeProjectsFragment(), false);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_home;
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
        getSupportFragmentManager().popBackStack();
        return super.onSupportNavigateUp();
    }
}