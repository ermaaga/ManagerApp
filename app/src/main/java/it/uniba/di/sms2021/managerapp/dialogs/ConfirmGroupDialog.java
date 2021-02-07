package it.uniba.di.sms2021.managerapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

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

import it.uniba.di.sms2021.managerapp.enitities.Exam;
import it.uniba.di.sms2021.managerapp.enitities.Group;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;

public class ConfirmGroupDialog  extends AppCompatDialogFragment {

    private FirebaseDatabase database;
    private DatabaseReference groupsRef;

    private Group group;

    public ConfirmGroupDialog(Group group) {
        this.group = group;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Informazione")
                .setMessage("Vuoi far parte di quest ogruppo studio ?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (updateMembers()){
                            CharSequence text = "Operazione avvenuta con successo, ora fai parte del gruppo: "+group.getName();
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getContext(), text, duration);
                            toast.show();
                        }else{
                            CharSequence text = "Errore durante l'inserimento al gruppo"+group.getName();
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getContext(), text, duration);
                            toast.show();
                        }

                    }
                });
        return builder.create();
    }

    private boolean updateMembers(){
        boolean result = false;
        try {
            HashMap childUpdates = new HashMap();

            List<String> currentMembers = getMembers(group.getId());
            if(!currentMembers.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                currentMembers.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
            }
            Log.i("count", String.valueOf(currentMembers));

            int count = 0;
            for(String item : currentMembers){
                Log.i("infos",String.valueOf(count));
                childUpdates.put(String.valueOf(count),item);
                count++;
            }

            //FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_GROUPS).child(group.getId()).child("membri")
              //      .updateChildren(childUpdates);
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
                    .addValueEventListener(new ValueEventListener() {
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
