package it.uniba.di.sms2021.managerapp.projects;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import it.uniba.di.sms2021.managerapp.R;

public class ProjectNoticesFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ProjectDetailActivity activity = (ProjectDetailActivity) getActivity();
        activity.setUpSearchAction(false, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_notices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}