package it.uniba.di.sms2021.managerapp.projects;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.ConnectionCheckBroadcastReceiver;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;
import it.uniba.di.sms2021.managerapp.utility.SearchUtil;

public class ProjectDetailActivity extends AbstractTabbedNavigationHubActivity {
    public static final int ABOUT_TAB_POSITION = 0;
    public static final int FILES_TAB_POSITION = 1;
    public static final int MEMBERS_TAB_POSITION = 2;
    public static final String INITIAL_TAB_POSITION_KEY = "initial_position";

    private static final int REQUEST_EVALUATION = 1;

    private static final String TAG = "ProjectDetailActivity";

    private Project project;

    private MenuItem searchMenuItem;
    private boolean searchActivated;
    // Listener custom implementato nei fragment al momento della loro creazione
    private SearchUtil.OnSearchListener onSearchListener;

    // Elementi della seach view presente nell'action bar
    private SearchView searchView;
    private HorizontalScrollView searchFilters;
    private SearchView.OnQueryTextListener queryTextListener;

    //Listener implementato nei fragment al momento della loro creazione
    private ConnectionCheckBroadcastReceiver.OnConnectionChangeListener connectionChangeListener;

    @Override
    protected Fragment getInitialFragment() {
        return new ProjectAboutFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        project = getIntent().getParcelableExtra(Project.KEY);
        int initialTab = getIntent().getIntExtra(INITIAL_TAB_POSITION_KEY, -1);

        if (initialTab != -1) {
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            TabLayout.Tab tab = tabLayout.getTabAt(initialTab);
            tabLayout.selectTab(tab);
            navigateToTabByPosition(initialTab);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(project.getName() + " - " + project.getStudyCaseName());
        actionBar.setSubtitle(project.getExamName());

        Log.i(TAG, project.toString());
        Log.i(TAG, "valutazione: "+project.getEvaluation());
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
        navigateToTabByPosition(tabPosition);
    }

    private void navigateToTabByPosition (int tabPosition) {
        if (tabPosition == ABOUT_TAB_POSITION) {
            navigateTo(new ProjectAboutFragment(), false);
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

        //Inizializzazione della barra di ricerca. La barra è solo attivata nei fragment che la richiedono.
        if (searchActivated) {
            searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchFilters = findViewById(R.id.searchFilterScrollView);
            setUpSearchBar();
        }

        MenuItem  evaluateMenuItem = menu.findItem(R.id.action_evaluate_project);
        if(project.isProfessor() && project.getEvaluation()==null){
            evaluateMenuItem.setVisible(true);

        }else{
            if(project.isProfessor() && project.getEvaluation()!=null){
                evaluateMenuItem.setTitle(R.string.text_label_update_evaluate);
                evaluateMenuItem.setVisible(true);
            }

        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuId = item.getItemId();
        MenuUtil.performMainActions(this, menuId);
        if (menuId == R.id.action_project_permissions) {
            Intent intent = new Intent(this, ProjectPermissionsActivity.class);
            intent.putExtra(Project.KEY, project);
            startActivity(intent);
        }else if (menuId == R.id.action_evaluate_project) {
            Intent intent = new Intent(this, ProjectEvaluationActivity.class);
            intent.putExtra(Project.KEY, project);
            startActivityForResult(intent, REQUEST_EVALUATION);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_EVALUATION && resultCode == RESULT_OK){
            project = data.getParcelableExtra(Project.KEY);
            invalidateOptionsMenu();
        }
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
    public void setUpSearchAction (boolean activated, @Nullable SearchUtil.OnSearchListener listener) {
        this.searchActivated = activated;
        this.onSearchListener = listener;
        invalidateOptionsMenu();
    }

    @Override
    protected void onConnectionUp() {
        super.onConnectionUp();
        if (connectionChangeListener != null) {
            connectionChangeListener.onConnectionUp();
        }
    }

    @Override
    protected void onConnectionDown() {
        super.onConnectionDown();
        if (connectionChangeListener != null) {
            connectionChangeListener.onConnectionDown();
        }
    }

    /**
     * Imposta le azioni da prendere nei singoli fragment qualora cambiasse lo stato della connessione.
     * Deve essere chiamato in onAttach in ogni fragment di questa activity.
     */
    public void setUpConnectionChangeListener (@Nullable ConnectionCheckBroadcastReceiver.OnConnectionChangeListener listener) {
        connectionChangeListener = listener;
    }

    private void setUpSearchBar() {
        //Inizializza la barra di ricerca ed i filtri
        SearchUtil.setUpSearchBar(this, searchView, searchFilters,
                R.string.text_hint_search_file, onSearchListener);
    }
}