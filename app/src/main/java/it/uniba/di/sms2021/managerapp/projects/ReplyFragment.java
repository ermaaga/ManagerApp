package it.uniba.di.sms2021.managerapp.projects;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Reply;
import it.uniba.di.sms2021.managerapp.enitities.Report;
import it.uniba.di.sms2021.managerapp.enitities.Review;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.LoginHelper;

public class ReplyFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    DatabaseReference replyReviewReference;
    DatabaseReference replyReportReference;
    FirebaseDatabase database;

    ImageButton sendButton;
    TextInputEditText reply_edit_text;
    TextInputLayout reply_text_input;

    String idgroup;
    String user;

    ProjectReviewsActivity activity;

    public ReplyFragment() {
        //Costruttore pubblico vuoto richiesto
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_reply, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDbHelper.getDBInstance();
        replyReviewReference = database.getReference(FirebaseDbHelper.TABLE_REPLIES_REVIEW);
        replyReportReference = database.getReference(FirebaseDbHelper.TABLE_REPLIES_REPORT);

        reply_edit_text = (TextInputEditText) view.findViewById(R.id.reply_edit_text);
        reply_text_input = (TextInputLayout) view.findViewById(R.id.reply_input_layout);

        sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);

        //codice per nascondere la keyboard quando si clicca fuori dall'editText
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboardFragment(view);
                return false;
            }
        });
    }

    public static void hideKeyboardFragment(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow( view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        if(reply_edit_text.length()>800) {
            reply_text_input.setError(getString(R.string.text_error_max_review_report));
        }else{
            Parcelable origin = this.getArguments().getParcelable("originReply");
            if(origin instanceof Review){
                user = LoginHelper.getCurrentUser().getAccountId();
                String replyComment = reply_edit_text.getText().toString();
                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                DatabaseReference newElement=replyReviewReference.push();
                Reply replyreview = new Reply(newElement.getKey(), user, date, replyComment, ((Review) origin).getReviewId());
                newElement.setValue(replyreview);

            }else if(origin instanceof Report){
                user = LoginHelper.getCurrentUser().getAccountId();
                String replyComment = reply_edit_text.getText().toString();
                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                DatabaseReference newElement=replyReportReference.push();
                Reply replyreport = new Reply(newElement.getKey(), user, date, replyComment, ((Report) origin).getReportId());
                newElement.setValue(replyreport);
            }


            Toast.makeText(requireContext(), R.string.text_message_review_reply_submitted, Toast.LENGTH_SHORT).show();
            //TODO inserire progress bar mentre ricarica la pagina?
            getActivity().recreate();
            dismiss();
        }


    }
}