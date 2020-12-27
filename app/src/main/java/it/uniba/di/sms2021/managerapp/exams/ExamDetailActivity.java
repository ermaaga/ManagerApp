package it.uniba.di.sms2021.managerapp.exams;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.utility.AbstractTabbedNavigationHubActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ExamDetailActivity extends AbstractTabbedNavigationHubActivity {
    private static final int STUDY_CASES_TAB_POSITION = 0;
    private static final int GROUPS_TAB_POSITION = 1;

    //I frammenti sottostanti useranno metodi presenti in questa classe che opereranno su
    // questo campo.
    private Exam exam;

    @Override
    protected Fragment getInitialFragment() {
        return new ExamStudyCasesFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO vedere se serve gestirlo in caso di cambi di configurazioni
        exam = getIntent().getExtras().getParcelable(ExamsActivity.CHOSEN_EXAM);
        Log.d("ExamDetailActivity", exam.toString());
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

    /**
     * Chiamato dal bottone di aggiunta casi di studio (nel fragment dei casi di studio)
     */
    public void addStudyCase(View view) {
        Toast.makeText(this, R.string.text_message_not_yet_implemented, Toast.LENGTH_SHORT).show();
    }
}