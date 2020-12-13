package it.uniba.di.sms2021.managerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        //TODO Fare login

        Intent intent = new Intent(this, DegreeCoursesActivity.class);
        startActivity(intent);
    }
}