package it.uniba.di.sms2021.managerapp.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import it.uniba.di.sms2021.managerapp.R;
import it.uniba.di.sms2021.managerapp.enitities.Department;
import it.uniba.di.sms2021.managerapp.enitities.ManagerFile;
import it.uniba.di.sms2021.managerapp.enitities.StudyCase;

public class DepartmentRecyclerAdapter extends ListAdapter<Department, RecyclerView.ViewHolder> {

    private Context context;
    private OnActionListener listener;

    public DepartmentRecyclerAdapter(Context context, OnActionListener listener) {
        super(new DiffCallback());
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_simple_string, parent, false);

        return new RecyclerView.ViewHolder(itemView) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;

        Department department = getItem(position);

        TextView departmentTextView = itemView.findViewById(R.id.simpleListItemTextView);
        MaterialCardView card = itemView.findViewById(R.id.simple_item_card);

        departmentTextView.setText(department.getName());

        if (department.isSelect()) {
            card.setCardBackgroundColor(context.getResources().getColor(R.color.lightGrey, context.getTheme()));
        } else {
            card.setCardBackgroundColor(context.getResources().getColor(R.color.white, context.getTheme()));
        }
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (department.isSelect()) {
                    card.setCardBackgroundColor(context.getResources().getColor(R.color.white, context.getTheme()));
                    department.setSelect(false);
                    if (selectedDepartments().size() == 0) {
                        listener.onSelectionAction(false);
                    }
                } else {
                    card.setCardBackgroundColor(context.getResources().getColor(R.color.lightGrey, context.getTheme()));
                    department.setSelect(true);
                    listener.onSelectionAction(true);
                }
            }
        });

    }

    static class DiffCallback extends DiffUtil.ItemCallback<Department> {

        @Override
        public boolean areItemsTheSame(@NonNull Department oldItem, @NonNull Department newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Department oldItem, @NonNull Department newItem) {
            return oldItem.equals(newItem);
        }
    }

    public List<String> selectedDepartments() {
        List<String> selectedDepartments = new ArrayList<>();
        for (Department depart : getCurrentList()) {
            if (depart.isSelect()) {
                selectedDepartments.add(depart.getId());
            }
        }
        return selectedDepartments;
    }


    public interface OnActionListener {
        void onSelectionAction(Boolean isSelected);
    }

}