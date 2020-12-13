package it.uniba.di.sms2021.managerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity con bottom navigation bar. Il file layout dell'activity deve necessariamente
 * includere il layout bottom_navigation con medesimo id.
 */
public abstract class AbstractBottomNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, HomeActivity.class));
        } else if (itemId == R.id.nav_exams) {
            startActivity(new Intent(this, ExamsActivity.class));
        } else if (itemId == R.id.nav_projects) {
            startActivity(new Intent(this, ProjectsActivity.class));
        }
        return false;
    }

    /**
     * Aggiorna l'item selezionato nel bottom navigation menù
     */
    private void updateNavigationBarState() {
        int actionId = getBottomNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        item.setChecked(true);
    }

    /**
     * Ritorna l'id del layout da associare all'activity
     */
    abstract int getLayoutId();

    /**
     * Ritorna l'id dell'elemento che dovrebbe essere selezionato nel menù di navigazione, quando
     * questa activity è attiva.
     */
    abstract int getBottomNavigationMenuItemId();
}
