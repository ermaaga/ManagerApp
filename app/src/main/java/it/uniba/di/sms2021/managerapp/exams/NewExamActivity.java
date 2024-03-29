package it.uniba.di.sms2021.managerapp.exams;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Course;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;
import it.uniba.di.sms2021.managerapp.utility.AbstractFormActivity;

public class NewExamActivity extends AbstractFormActivity {


    private  int yearCounter = 3;
    private TextInputEditText examName;
    private TextInputLayout tlExamName,tlExamYear;
    private AutoCompleteTextView examYear;
    private AutoCompleteTextView examCourse;
    private Button bt_create_exam;

    private FirebaseDatabase database;
    private DatabaseReference examsRef;
    private Course chosenCourse = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_exam;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
        //initialize global variables
        initialize();

        //If click on button
       bt_create_exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_examName = examName.getText().toString();
                String text_examYear = examYear.getText().toString();

                if(everyThingOk(text_examName,text_examYear, chosenCourse)){
                   createExam(text_examName,text_examYear, chosenCourse);
                }
            }
        });

    }

    private boolean everyThingOk(String examName, String examYear, Course chosenCourse){
        boolean result = false;

        try {
            if (!(TextUtils.isEmpty(examName) || TextUtils.isEmpty(examYear)
                    || chosenCourse == null)) {
                if (examName.length()<tlExamName.getCounterMaxLength()){
                    result = true;
                }else{
                    tlExamName.setError(getString(R.string.text_error_validation,
                            tlExamName.getCounterMaxLength()));
                }

            } else {
                String error = getString(R.string.label_error_fields);
                Toast.makeText(NewExamActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    private void initialize(){
        //EditText
        examName = findViewById(R.id.txt_exam_name);
        examYear = findViewById(R.id.atc_exam_year);
        examCourse = findViewById(R.id.atc_exam_degree_course);

        //Layout
        tlExamName =findViewById(R.id.tli_exam_name);
        tlExamYear =findViewById(R.id.tli_exam_year);

        bt_create_exam = findViewById(R.id.btn_create_NewExam);

        database = FirebaseDbHelper.getDBInstance();
        examsRef = database.getReference(FirebaseDbHelper.TABLE_EXAMS);

        //Add items to list
        //DropDown menu
        List<String> years = new ArrayList<String>();
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR) + 2;
        while(yearCounter > 0){
            String yearInString = String.valueOf(year);
            years.add(yearInString);
            year--;
            yearCounter--;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                NewExamActivity.this,
                R.layout.dropdown_item,
                years
        );
        examYear.setAdapter(adapter);

        FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_COURSES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Course> courses = new ArrayList<>();
                        for (String courseId: LoginHelper.getCurrentUser().getCorsi()) {
                            Course course = snapshot.child(courseId).getValue(Course.class);
                            courses.add(course);
                        }

                        examCourse.setAdapter(new ArrayAdapter<>(NewExamActivity.this,
                                R.layout.dropdown_item, courses));
                        examCourse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                chosenCourse = (Course) parent.getAdapter().getItem(position);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void createExam(String text_examName, String text_examYear, Course chosenCourse){

        DatabaseReference newElement = examsRef.push();

        int parsedYear = Integer.parseInt(text_examYear);
        List<String> professors = new ArrayList<>();
        List<String> studens = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        professors.add(userId);

        Exam exam = new Exam(newElement.getKey(), text_examName,professors,studens,
                chosenCourse.getId(), parsedYear);
        newElement.setValue(exam, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                System.err.println("Value was set. Error = "+error);
                if (error == null) {
                    Toast.makeText(getApplicationContext(), R.string.text_message_exam_created, Toast.LENGTH_SHORT).show();
                    NewExamActivity.super.onBackPressed();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.creation_exam_failed, Toast.LENGTH_SHORT).show();
                    NewExamActivity.super.onBackPressed();
                }
            }
        });

    }
    public void onDecline(View view) {

    }

    public void onAccept(View view) {

    }
}