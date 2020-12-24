package it.uniba.di.sms2021.managerapp.projects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.utility.AbstractBottomNavigationActivity;
import it.uniba.di.sms2021.managerapp.utility.MenuUtil;

public class ProjectsActivity extends AbstractBottomNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_projects;
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

    public void goToProject1(View view) {
        startActivity(new Intent(this, ProjectDetailActivity.class));
    }
}