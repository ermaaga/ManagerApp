package it.uniba.di.sms2021.managerapp.projects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProjectDetailActivity extends AbstractTabbedNavigationHubActivity {
    private static final int NOTICES_TAB_POSITION = 0;
    private static final int FILES_TAB_POSITION = 1;
    private static final int MEMBERS_TAB_POSITION = 2;

    private static final String TAG = "ProjectDetailActivity";

    private Project project;

    private MenuItem searchMenuItem;
    private boolean searchActivated;
    private OnSearchListener onSearchListener;

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
        getMenuInflater().inflate(R.menu.menu_project_detail, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setVisible(searchActivated);

        //Impostazioni per la barra di ricerca. La barra è solo attivata nei fragment che la richiedono.
        if (searchActivated) {
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchView.setIconifiedByDefault(true);
            searchView.setQueryHint(getString(R.string.text_hint_search_file));
            searchView.setInputType(InputType.TYPE_CLASS_TEXT);

            //Quando un utente digita qualcosa nella barra, il fragment decide che azioni prendere.
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    onSearchListener.onSearchAction(query);

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    onSearchListener.onSearchAction(newText);

                    return false;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);
        if (menuId == R.id.action_project_permissions) {
            Intent intent = new Intent(this, ProjectPermissionsActivity.class);
            intent.putExtra(Group.Keys.GROUP, project.getGroup());
            startActivity(intent);
        }

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

    /**
     * Imposta la visibilità e l'azione dell'azione di ricerca nello specifico fragment.
     * Deve essere chiamato in onAttach in ogni fragment dell'activity.
     * @param activated se l'azione di ricerca è attivata nel fragment corrente
     * @param listener cosa fà l'azione di ricerca nel fragment corrente
     */
    public void setUpSearchAction (boolean activated, @Nullable OnSearchListener listener) {
        this.searchActivated = activated;
        this.onSearchListener = listener;
        invalidateOptionsMenu();
    }

    public interface OnSearchListener {
        void onSearchAction(String query);
    }
}