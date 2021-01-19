package it.uniba.di.sms2021.managerapp.exams;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class NewExamActivity extends AbstractFormActivity {

    private TextView mTextView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_exam;
    }

    @Override
    protected int getBottomNavigationMenuItemId() {
        return R.id.nav_exams;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTextView = (TextView) findViewById(R.id.text);

    }

    public void onDecline(View view) {

    }

    public void onAccept(View view) {

    }
}