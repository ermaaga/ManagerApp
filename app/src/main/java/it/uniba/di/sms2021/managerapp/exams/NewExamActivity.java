package it.uniba.di.sms2021.managerapp.exams;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import it.uniba.di.sms2021.managerapp.R;
import androidx.appcompat.app.AppCompatActivity;
public class NewExamActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exam);

        mTextView = (TextView) findViewById(R.id.text);

    }

    public void onDecline(View view) {

    }

    public void onAccept(View view) {

    }
}