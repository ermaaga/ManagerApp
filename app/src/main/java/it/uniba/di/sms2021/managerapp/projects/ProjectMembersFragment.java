package it.uniba.di.sms2021.managerapp.projects;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.ProfileActivity;
import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.User;
import it.uniba.di.sms2021.managerapp.firebase.FirebaseDbHelper;
import it.uniba.di.sms2021.managerapp.firebase.Project;
import it.uniba.di.sms2021.managerapp.lists.UserRecyclerAdapter;

public class ProjectMembersFragment extends Fragment {

    private RecyclerView groupMembersRecyclerView;
    private UserRecyclerAdapter adapter;

    private ValueEventListener userListner;
    private DatabaseReference userReferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ProjectDetailActivity activity = (ProjectDetailActivity) requireActivity();
        activity.setUpSearchAction(false, null);
        activity.setUpConnectionChangeListener(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
     View itemView = inflater.inflate(R.layout.fragment_project_members,container,false);
     groupMembersRecyclerView = itemView.findViewById(R.id.members_recyclerView);
      return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new UserRecyclerAdapter(getContext(), new UserRecyclerAdapter.OnActionListener(){
            @Override
            public void onItemClicked(User string) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra(User.KEY, string);
                boolean fromLink = true;
                intent.putExtra("fromLinkBoolean", fromLink);
                startActivity(intent);
            }
        });

        groupMembersRecyclerView.setAdapter(adapter);
        groupMembersRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL));
        groupMembersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Project selectedProject = ((ProjectDetailActivity) getActivity()).getSelectedProject();
        List<String> lstMembers = selectedProject.getMembri();

        userListner = new ValueEventListener() {
            List<User> lstUsers = new ArrayList<>();
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String userId: lstMembers) {
                    User user = snapshot.child(userId).getValue(User.class);
                    lstUsers.add(user);
                }

                adapter.setAsLeader(lstMembers.get(0));
                adapter.submitList(lstUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        userReferences = FirebaseDbHelper.getDBInstance().getReference(FirebaseDbHelper.TABLE_USERS);
        userReferences.addValueEventListener(userListner);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        userReferences.removeEventListener(userListner);
    }
}