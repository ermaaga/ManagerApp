package it.uniba.di.sms2021.managerapp.exams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class NewGroupActivity extends AbstractBottomNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_clear_24);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_group;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
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
        finish();
        return super.onSupportNavigateUp();
    }
}