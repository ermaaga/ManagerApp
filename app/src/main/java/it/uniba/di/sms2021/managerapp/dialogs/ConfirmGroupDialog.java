package it.uniba.di.sms2021.managerapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.GroupJoinRequest;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.exams.ExamDetailActivity;
import it.uniba.di.sms2021.managerapp.exams.ExamGroupsFragment;
import it.uniba.di.sms2021.managerapp.exams.ExamsActivity;
import it.uniba.di.sms2021.managerapp.exams.NewExamActivity;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class ConfirmGroupDialog  extends AppCompatDialogFragment {

    private FirebaseDatabase database;
    private DatabaseReference groupsRef;
    private Context context;

    private Group group;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Se è true informerà l'utente che è necessario unirsi al gruppo per visualizzarlo
    private boolean joinNecessary;

    public ConfirmGroupDialog(Context context, Group group, boolean joinNecessary) {
        this.context = context;
        this.group = group;
        this.joinNecessary = joinNecessary;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Mostra un messaggio aggiuntivo qualora sia necessario unirsi al gruppo per visualizzarlo
        int messageRes = joinNecessary ? R.string.label_Dialog_confimation_message_join_necessary
                : R.string.label_Dialog_confimation_message;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.label_Dialog_Header))
                .setMessage(messageRes)
                .setNegativeButton(R.string.label_Dialog_declination, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.label_Dialog_confimation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendRequest();

                        /*
                        if (updateMembers()){
                            CharSequence text = R.string.label_Dialog_success + group.getName();
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getContext(), text, duration);
                            toast.show();
                        }else{
                            CharSequence text = R.string.label_Dialog_failed + group.getName();
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getContext(), text, duration);
                            toast.show();
                        }
                         */

                    }
                });

        return builder.create();
    }

    private void sendRequest() {
        DatabaseReference requestReference = FirebaseDbHelper.getDBInstance()
                .getReference(FirebaseDbHelper.TABLE_GROUP_REQUESTS).push();

        requestReference.setValue(new GroupJoinRequest(requestReference.getKey(), currentUserId,
                group.getId(), group.getMembri().get(0)))
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, R.string.text_message_group_request_sent,
                            Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, R.string.text_message_group_request_failed,
                            Toast.LENGTH_LONG).show();
                }
            });
    }

    private boolean updateMembers(){
        boolean result = false;
        try {
            List<String> members = group.getMembri();
            if (members == null) {
                members = new ArrayList<>();
            }
            members.add(currentUserId);
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                    .child(group.getId()).child(Group.Keys.MEMBERS).setValue(members, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    System.err.println("Value was set. Error = "+error);
                }
            });
            result = true;

        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    private List<String> getMembers(String groupId) {
        List<Group> lstGroups = new ArrayList<>();
        List<String> lstGroupMembers = new ArrayList<>();
        try {
            FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot child: snapshot.getChildren()) {
                                Group currentGroups = child.getValue(Group.class);

                                if (currentGroups.getId().equals(groupId)) {
                                    lstGroups.add(currentGroups);
                                }
                            }
                            Log.i("groups", String.valueOf(lstGroups));
                            for(Group itemGroup : lstGroups){
                                for(String member : itemGroup.getMembri()){
                                    Log.i("member", member);
                                    lstGroupMembers.add(member);
                                    Log.i("memberin", String.valueOf(lstGroupMembers));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("error","errore in getStudyCasesGroup" );
        }
        Log.i("memberout", String.valueOf(lstGroupMembers));
        return  lstGroupMembers;

    }


}
